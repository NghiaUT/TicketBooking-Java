# ==========================================
# Giai đoạn 1: Build source code với Maven
# ==========================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy file cấu hình dependency để tận dụng Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy toàn bộ mã nguồn và đóng gói file JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# Giai đoạn 2: Runtime image tối ưu cho Render
# ==========================================
FROM eclipse-temurin:21-jre-alpine

# Tạo user không có quyền root để tăng tính bảo mật
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy artifact đã build từ giai đoạn builder
COPY --from=builder /build/target/*.jar app.jar

# Phân quyền cho user
RUN chown -R appuser:appgroup /app

USER appuser

# Render tự động inject biến môi trường PORT (mặc định fallback về 8080)
ENV PORT=8080
EXPOSE 8080

# Cấu hình tối ưu bộ nhớ JVM cho Render (đặc biệt phù hợp gói Free tier 512MB RAM)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:+ExitOnOutOfMemoryError"

# Khởi chạy ứng dụng và lắng nghe trên port được Render cấp phát
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
