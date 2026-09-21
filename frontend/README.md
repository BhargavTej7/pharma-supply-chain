The frontend is served by the Compose Nginx container at `http://localhost:3000` and calls the API Gateway at `http://localhost:8080`.

Features include catalog search/filter/sort, medicine details, a local cart, real order checkout, customer order history, logout, and an administrator medicine/order view. For another environment, set `window.API_BASE` before `app.js` loads to point at the deployed Gateway URL.
