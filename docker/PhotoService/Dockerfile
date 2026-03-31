FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
RUN mkdir photos
COPY Services/PhotoService/target/*.jar app.jar
EXPOSE 9030
ENTRYPOINT ["mkdir", "photos"]
ENTRYPOINT ["java","-jar","app.jar"]