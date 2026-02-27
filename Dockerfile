# Stage 1: Build
FROM eclipse-temurin:22-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:22-jre-jammy
WORKDIR /app
COPY --from build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]