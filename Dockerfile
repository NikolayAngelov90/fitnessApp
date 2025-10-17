FROM mcr.microsoft.com/openjdk/jdk:21

COPY target/fitnessapp-*.jar app.jar

LABEL authors="nikit"

ENTRYPOINT ["top", "-b"]