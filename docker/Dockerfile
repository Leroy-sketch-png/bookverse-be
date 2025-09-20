# ----------------------------------------------------------------------------------------------------------------------
# Build Stage: This stage compiles your Spring Boot application into a fat JAR. It uses a Maven base image.
# ----------------------------------------------------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS build
# Set the working directory inside the container
WORKDIR /build

# Copy the pom.xml file to download dependencies. This is a layer that changes infrequently and can be cached.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the application, skipping tests.
COPY src ./src
RUN mvn clean package -DskipTests

# ----------------------------------------------------------------------------------------------------------------------
# Runtime Stage: This stage creates the final, lightweight image. It copies only the necessary JAR from the build stage.
# ----------------------------------------------------------------------------------------------------------------------
FROM amazoncorretto:21
# Expose the port that the Spring Boot application listens on. This is a declaration, not a functional mapping.
EXPOSE 8080

# Define build arguments that will be passed in during the docker build command.
ARG PROFILE=dev
ARG APP_VERSION=0.0.1

# Set the working directory for the runtime container.
WORKDIR /app

# Copy the built JAR file from the 'build' stage into this final image.
COPY --from=build /build/target/bookverseserver-*.jar /app/app.jar

# Define environment variables that your Spring Boot application will use.
ENV DB_URL=jdbc:postgresql://postgres-sql-bvs:5432/bookversedb
ENV ACTIVE_PROFILE=${PROFILE}
ENV JAR_VERSION=${APP_VERSION}

# The command to run when the container starts. It activates the Spring profile and sets the database URL.
CMD ["java", "-Dspring.profiles.active=${ACTIVE_PROFILE}", "-Dspring.datasource.url=${DB_URL}", "-jar", "app.jar"]
