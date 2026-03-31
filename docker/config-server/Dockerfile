FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY Services/config-server/target/*.jar app.jar
EXPOSE 9999
ENTRYPOINT ["java","-jar","app.jar"]