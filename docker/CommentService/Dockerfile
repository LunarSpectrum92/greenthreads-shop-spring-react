FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY Services/CommentService/target/*.jar app.jar
EXPOSE 9040
ENTRYPOINT ["java","-jar","app.jar"]