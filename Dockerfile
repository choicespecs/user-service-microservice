FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y postgresql-client

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]