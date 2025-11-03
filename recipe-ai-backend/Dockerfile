# --- 階段 1: 建構 (Build) ---
# 使用 eclipse-temurin 的 JDK 17 (包含 Maven) 作為 "builder"
# 這取代了 'maven:3.8.5-openjdk-17'
FROM eclipse-temurin:17-jdk-jammy as builder

# 設定工作目錄
WORKDIR /app

# 複製 Maven  wrapper 和 pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# (可選) 只下載依賴，利用 Docker 快取
# RUN ./mvnw dependency:go-offline

# 複製所有原始碼
COPY src/ ./src
# 打包應用程式，跳過測試
RUN ./mvnw clean package -DskipTests

# --- 階段 2: 運行 (Run) ---
# 使用 eclipse-temurin 的 JRE (輕量版) 作為 "runner"
# 這取代了 'openjdk:17-jre-slim'
FROM eclipse-temurin:17-jre-jammy

# 設定工作目錄
WORKDIR /app

# 從 "builder" 階段複製打包好的 .jar 檔案
# !! 注意: 這裡的路徑 /app/target/*.jar 必須匹配您的 .jar 檔名 !!
COPY --from=builder /app/target/*.jar app.jar

# 聲明 Port
EXPOSE 8080

# 容器啟動指令 (已修正空格)
ENTRYPOINT ["java","-jar","app.jar"]