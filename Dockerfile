# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy just build metadata first to leverage Docker cache
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Pre-fetch dependencies to speed up subsequent builds
RUN ./mvnw -B -q -DskipTests dependency:go-offline

# Now copy sources and build
COPY src ./src
RUN ./mvnw -B -DskipTests package

# ---------- Runtime stage ----------
FROM openjdk:17-jdk-slim
WORKDIR /app

# (Optional) Tools you had before
RUN apt-get update && apt-get install -y --no-install-recommends postgresql-client \
  && rm -rf /var/lib/apt/lists/*

# Copy the fat JAR from the build stage
# (This grabs the single jar your build produced, whatever its name is.)
COPY --from=build /app/target/*.jar /app/app.jar

# Run it
ENTRYPOINT ["java","-jar","/app/app.jar"]