FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY Services/OrderService/target/*.jar app.jar
EXPOSE 9050
ENTRYPOINT ["java","-jar","app.jar"]