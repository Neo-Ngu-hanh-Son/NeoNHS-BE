# Stage 1: Build bằng Maven với JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Chạy ứng dụng với JRE 21 
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Lấy file jar từ stage build
COPY --from=build /app/target/NeoNHS-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]