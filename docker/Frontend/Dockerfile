FROM node:24-bookworm AS build
WORKDIR /app
COPY FrontendService/ztpai-react-frontend/package*.json ./
RUN npm install
COPY FrontendService/ztpai-react-frontend/ .
RUN npm run build

FROM nginx:alpine
COPY docker/Frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]