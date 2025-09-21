# ----------------------------------------------------------------------------------------------------------------------
# Build Stage: Compile Spring Boot app into fat JAR
# ----------------------------------------------------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy pom.xml and download dependencies first (caching layer)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ----------------------------------------------------------------------------------------------------------------------
# Runtime Stage: lightweight container
# ----------------------------------------------------------------------------------------------------------------------
FROM amazoncorretto:21
EXPOSE 8080

ARG PROFILE=prod
ARG APP_VERSION=0.0.1
WORKDIR /app

# Copy built JAR
COPY --from=build /build/target/bookverseserver-*.jar /app/app.jar

# Application profile and version
ENV ACTIVE_PROFILE=${PROFILE}
ENV JAR_VERSION=${APP_VERSION}

# DB credentials are set via Render Environment Variables
#CMD ["java", \
#     "-Dspring.profiles.active=${ACTIVE_PROFILE}", \
#     "-Dspring.datasource.url=jdbc:postgresql://db.mwxvoifusflwtmxxiybq.supabase.co:5432/postgres", \
#     "-Dspring.datasource.username=postgres", \
#     "-Dspring.datasource.password=votrungtin2005", \
#     "-jar", "app.jar"]

CMD ["java", "-Dspring.profiles.active=${ACTIVE_PROFILE}", "-jar", "app.jar"]
