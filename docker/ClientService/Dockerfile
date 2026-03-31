FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY Services/clientService/target/*.jar app.jar
EXPOSE 9010
ENTRYPOINT ["java","-jar","app.jar"]