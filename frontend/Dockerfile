FROM nginx:1.27-alpine
COPY . /usr/share/nginx/html
ARG API_BASE_URL=http://localhost:8080
RUN sed -i "s|http://localhost:8080|${API_BASE_URL}|g" /usr/share/nginx/html/index.html /usr/share/nginx/html/app.js
EXPOSE 80
