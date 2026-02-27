# Stage 1: Build
FROM eclipse-temurin:22-jdk-jammy AS build
WORKDIR /app
COPY . .
# Using chmod to ensure the wrapper is executable on the Linux build server
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:22-jre-jammy
WORKDIR /app
# Fixed the --from syntax
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]