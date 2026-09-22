FROM nginx:1.27-alpine
COPY index.html app.js styles.css /usr/share/nginx/html/
COPY default.conf.template /etc/nginx/templates/default.conf.template
ARG API_BASE_URL=http://localhost:8080
ENV API_BASE_URL=${API_BASE_URL}
ENV PORT=80
ENV NGINX_ENVSUBST_FILTER=PORT
RUN sed -i "s|http://localhost:8080|${API_BASE_URL}|g" /usr/share/nginx/html/index.html /usr/share/nginx/html/app.js
EXPOSE 80
