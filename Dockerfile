FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY app/fitnessapp-*.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]