FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY Services/ProductService/target/*.jar app.jar
EXPOSE 9020
ENTRYPOINT ["java","-jar","app.jar"]