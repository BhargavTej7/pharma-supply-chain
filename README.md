# PS007 - Apothecary Express

Apothecary Express is an e-pharmaceutical supply-chain and order-fulfillment platform for browsing medicines, managing inventory, authenticating customers and administrators, and placing orders with conditional stock reduction.

## Architecture

```text
Browser :3000
    |
API Gateway :8080
    |
Eureka :8761 ---- User Service :8081 ---- PostgreSQL
              \-- Medicine Service :8082 -/
              \-- Order Service :8083 --- Medicine Service
```

The services communicate with Docker service names inside Compose. The frontend calls only the API Gateway.

## Services and technology

- Frontend: semantic HTML, CSS, vanilla JavaScript, Nginx
- API Gateway: Spring Cloud Gateway WebFlux and Eureka discovery
- User Service: registration, BCrypt passwords, JWT issuance, role authorization
- Medicine Service: catalog CRUD and atomic stock reduction
- Order Service: customer orders, ownership checks, status management, medicine integration
- Eureka Server: service registration and discovery
- PostgreSQL 16: persistent users, medicines, and orders
- Java 21, Spring Boot 4.1.1, Spring Cloud 2025.1.0, Maven, Docker Compose

## Ports

| Component | Port |
| --- | ---: |
| Frontend | 3000 |
| API Gateway | 8080 |
| User Service | 8081 |
| Medicine Service | 8082 |
| Order Service | 8083 |
| Eureka | 8761 |
| PostgreSQL | 5432 |

Use `http://localhost:8080` for application APIs and `http://localhost:3000` for the frontend.

## Configuration

Copy `.env.example` to `.env` and provide private values. Never commit `.env`.

Required values:

- `DATABASE_URL`: PostgreSQL JDBC URL
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET`: random HMAC secret of at least 32 bytes
- `INTERNAL_SERVICE_TOKEN`: private token shared only by Order and Medicine Services
- `EUREKA_URL`
- `MEDICINE_SERVICE_URL`
- `FRONTEND_ORIGIN`
- `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD`: optional local bootstrap pair; configure together only
- `FRONTEND_API_BASE_URL`: Gateway URL baked into a production frontend image

Compose requires `DATABASE_PASSWORD`, `JWT_SECRET`, and `INTERNAL_SERVICE_TOKEN`; it no longer silently falls back to development secrets.

## Running with Docker

```powershell
Copy-Item .env.example .env
# Edit .env with local-only values
 docker compose config
 docker compose build
 docker compose up -d
 docker compose ps
```

Check Eureka at `http://localhost:8761`. Check service health where Actuator is enabled at `/actuator/health`.

## API overview

### Authentication

- `POST /users` or `POST /auth/register`: public customer registration
- `POST /users/login` or `POST /auth/login`: public login and JWT issuance
- `GET /users`, `GET /users/{id}`, `PUT /users/{id}`, `DELETE /users/{id}`: administrator operations

Registration always creates `CUSTOMER`; clients cannot self-register as `ADMIN`.

### Initial administrator bootstrap

The User Service supports a one-time, startup-based administrator bootstrap. Set `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` together in the deployment secret configuration before the first start. The password is BCrypt-hashed by the existing password encoder. If the email already belongs to an ADMIN, startup is idempotent and leaves the account unchanged. If it belongs to a non-admin or only one variable is supplied, startup fails instead of changing an existing account or creating an ambiguous configuration. The bootstrap values are never logged or returned by an API. Remove or clear the bootstrap secret after the initial administrator has been created.

### Medicines

- `GET /medicines`: public catalog
- `GET /medicines/{id}`: public medicine details
- `POST /medicines`: administrator only
- `PUT /medicines/{id}`: administrator only
- `DELETE /medicines/{id}`: administrator only
- `POST /medicines/{id}/stock/reduce?quantity=N`: internal service token only

Send `Authorization: Bearer <jwt>` for protected calls. The internal stock operation uses `X-Internal-Service-Token` and is not a public customer/admin operation.

### Orders

- `POST /orders`: authenticated customer or administrator; uses the JWT user ID
- `GET /orders/{id}`: owner or administrator
- `GET /orders/user/{userId}`: owner or administrator
- `GET /orders`: administrator only
- `PUT /orders/{id}/status`: administrator only
- `DELETE /orders/{id}`: administrator only

Order creation asks Medicine Service to execute `stock = stock - quantity` only where sufficient stock exists. A failed conditional update returns a business conflict and the order is not persisted. The current cross-service flow is not a distributed transaction: a database failure after successful stock reduction requires a future reservation/compensation or outbox improvement.

## Frontend

The static frontend is served by Nginx and uses `window.API_BASE` when supplied, defaulting to `http://localhost:8080` for local development. It includes:

- Apothecary Express landing experience
- Catalog search, category filtering, sorting, and stock display
- Medicine details and quantity selection
- Local cart with quantity controls and subtotal
- Real checkout through the Order Service via the Gateway
- Customer order history and account dashboard
- Admin medicine create, update, and delete view for ADMIN JWTs
- Responsive layout, loading/error/empty states, escaped API content, and logout

For a deployed frontend, inject `window.API_BASE` before `app.js` or provide the equivalent platform configuration with the public Gateway URL.

## Database and migrations

Development currently uses `spring.jpa.hibernate.ddl-auto=update` to preserve the existing local workflow. A production baseline is prepared at `database/migrations/V1__initial_schema.sql` for Flyway or an equivalent migration runner. Production should run migrations first and switch Hibernate schema management to validation/none after the existing database has been baselined.

The migration creates users, medicines, and orders tables with non-negative stock, valid roles, positive quantities, monetary checks, and lookup indexes. It is intentionally not wired into the development containers yet.

## Testing

Run the existing service suites:

```powershell
Push-Location user-service; .\mvnw.cmd test; Pop-Location
Push-Location medicine-service\medicine-service; .\mvnw.cmd test; Pop-Location
Push-Location user-service; .\mvnw.cmd -f ..\order-service\pom.xml test; Pop-Location
Push-Location user-service; .\mvnw.cmd -f ..\api-gateway\pom.xml test; Pop-Location
Push-Location user-service; .\mvnw.cmd -f ..\eureka-server\pom.xml test; Pop-Location
```

Manual acceptance should cover registration, login, BCrypt storage, missing/invalid JWTs, customer/admin permissions, medicine CRUD, successful stock reduction, insufficient stock, order ownership, internal-token protection, Gateway routing, and the frontend workflow.

## Security and deployment notes

- Rotate any credentials that have existed in local `.env` files before production.
- Store secrets in the deployment platform's secret manager.
- Do not publish `.env`, database credentials, JWT secrets, or internal tokens.
- Keep the Gateway as the public API boundary; internal service ports can be removed from production Compose when operational access is not required.
- Configure CORS to the deployed frontend origin, not `*`.
- Use managed PostgreSQL and a migration runner in production.
- Eureka is suitable for this current Spring Cloud architecture, but a platform-native service discovery layer may be preferable in a managed deployment.

## Deployment preparation

The project is container-ready for a platform that supports multiple Java services, Docker images, PostgreSQL, private networking, and environment variables. Create one deployment unit for each service, a managed PostgreSQL database, and a public Gateway/frontend pair. Configure:

- Public frontend URL and `FRONTEND_ORIGIN`
- Public Gateway URL for frontend `API_BASE`
- Private `EUREKA_URL`
- Private `MEDICINE_SERVICE_URL`
- Managed database connection variables
- Rotated `JWT_SECRET` and `INTERNAL_SERVICE_TOKEN`

Deployment still requires platform account authorization, secret entry, networking, and live-site verification; those actions cannot be completed without the target platform and credentials.

For a Docker host with private service networking, use the production override:

```powershell
docker compose --env-file .env.production -f docker-compose.production.yml config
docker compose --env-file .env.production -f docker-compose.production.yml build
docker compose --env-file .env.production -f docker-compose.production.yml up -d
```

The production file is standalone and expects managed PostgreSQL; it does not start the local PostgreSQL container. Production configuration requires managed PostgreSQL values, rotated JWT and internal-service secrets, `EUREKA_HOST`, `FRONTEND_ORIGIN`, `FRONTEND_API_BASE_URL`, `EUREKA_URL`, `MEDICINE_SERVICE_URL`, and the initial admin bootstrap pair. Internal services are kept on a private Compose network; only the Gateway and frontend are published.
