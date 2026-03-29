FROM openjdk:21-jdk-alpine
WORKDIR /app
COPY Services/ApiGateway/target/*.jar app.jar
EXPOSE 8222
ENTRYPOINT ["java","-jar","/app.jar"]