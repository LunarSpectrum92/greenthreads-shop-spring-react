FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY Services/PaymentService/target/*.jar app.jar
EXPOSE 9060
ENTRYPOINT ["java","-jar","app.jar"]