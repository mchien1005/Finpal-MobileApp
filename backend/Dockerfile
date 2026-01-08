# Dockerfile cho Backend Java Spring Boot
# Multi-stage build để giảm kích thước image

# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml và download dependencies trước (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Cài đặt MySQL client tools (mysqldump) cho chức năng backup database
# Và wget cho health check
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        default-mysql-client \
        wget \
        fonts-noto \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

# Copy JAR file từ build stage
COPY --from=build /app/target/*.jar app.jar

# Set timezone to Vietnam (UTC+7)
# Quan trọng: Server ở Singapore (UTC+8), nhưng cần chạy theo giờ Việt Nam
ENV TZ=Asia/Ho_Chi_Minh
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Expose port
EXPOSE 8080

# Health check - dùng endpoint có sẵn thay vì actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/auth/test || exit 1

# Run application với timezone Vietnam
ENTRYPOINT ["java", "-Duser.timezone=Asia/Ho_Chi_Minh", "-jar", "app.jar"]

