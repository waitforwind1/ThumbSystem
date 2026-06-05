FROM eclipse-temurin:21-jre
WORKDIR /app
EXPOSE 8082
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar","app.jar"]

LABEL authors="22097"

