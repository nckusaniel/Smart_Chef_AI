# 🍳 Smart Chef AI - AI 食譜推薦系統

Smart Chef AI 是一個全端應用程式，旨在解決「我手邊有這些食材，但我不知道能做什麼？」的日常難題。

本系統使用 **Java Spring Boot** 作為後端，**React.js** 作為前端。使用者只需輸入**現有食材**和**料理風格**（例如：低碳、泰式），系統將即時生成包含詳細步驟、食材清單的客製化食譜，並**同步生成一道AI 料理圖片**。

![image](https://github.com/nckusaniel/java_project/blob/master/flowchart.png)
<img width="2039" height="123" alt="image" src="https://github.com/user-attachments/assets/7ba89d6f-0cb2-4b9b-8097-120be652d3ce" />


## ✨ 核心功能

* **AI 服務編排**： 後端服務會協調多個 AI 模型，一個用於生成結構化食譜 (JSON)，另一個（Google Gemini 2.5 Flash）專門用於生成高品質的料理圖片。
* **智能資料庫快取**： 為了降低 AI API 成本並加速回應，所有生成過的食譜都會被快取在 MySQL 資料庫中。
* **快取鍵正規化**： 獨特的演算法會將使用者的輸入（如 "雞肉, 洋蔥" 和 "洋蔥, 雞肉"）正規化為相同的快取鍵，大幅提高快取命中率。
* **全端分離架構**： 採用 Spring Boot REST API + React.js 的前後端分離架構。
* **容器化支援**： 內建多階段 `Dockerfile`，可快速建置輕量化的 JRE 映像檔以便部署。

## 🏛️ 系統架構

1.  **Client (React)**： 使用者在 `App.js` 介面輸入食材和風格，透過 `api.js` 向後端發送 `POST` 請求。
2.  **Backend (Spring Boot)**：
    * `RecipeController` 接收請求，並轉交給 `RecipeService`。
    * `RecipeService` 首先對輸入進行**正規化**（轉小寫、排序、去除空格）以產生一個唯一的 `key_id`。
    * 服務使用 `key_id` 查詢 `RecipeCacheRepository` (MySQL 資料庫)。
    * **[快取命中 Cache Hit]**：如果資料庫中存在該 `key_id`，則直接從資料庫讀取 `RecipeCache` 實體，轉換為 `RecipeResponse` 並立即回傳。
    * **[快取錯失 Cache Miss]**：
        1.  呼叫 `ChatModel` (Spring AI) 生成食譜的結構化 JSON（標題、食材、步驟）。
        2.  呼叫 `GeminiImageService`（使用 `RestTemplate`）將食譜步驟作為提示詞，生成一張 Base64 圖片 URL。
        3.  將完整的食譜和圖片 URL 存入 `RecipeCache` 資料庫。
        4.  回傳 `RecipeResponse` 給前端。
3.  **Exception Handling**： 任何在服務層發生的 `ApiException`（如 AI 呼叫失敗）都會被 `GlobalExceptionHandler` 捕捉，並回傳標準化的 JSON 錯誤訊息給前端。

## 🛠️ 技術棧 (Tech Stack)

| 類別 | 技術/工具 | 備註 |
| :--- | :--- | :--- |
| **後端** | Java 17, Spring Boot 3.x | 核心服務框架 |
| **AI 服務** | Spring AI (Gemini) | 用於生成 JSON 格式的食譜文字 |
| **AI 圖片** | Google Gemini 2.5 Flash | 透過 RestTemplate 客製化呼叫，生成圖片 |
| **資料庫** | Spring Data JPA, MySQL | 用於快取 AI 生成的結果 |
| **前端** | React.js | UI 介面 |
| **DevOps** | Docker (多階段建置) | 容器化部署 |
| **建構工具** | Maven (後端), NPM (前端) | 專案依賴管理 |

## 🚀 專案啟動

### 1. 環境設定

在專案的 **`recipe-ai-backend`** 資料夾底下，建立一個 `.env` 檔案。 (根目錄的 `.gitignore` 已設定會忽略此檔案，請安心填寫)。

```.env
# application.properties 會讀取這些變數

# 您的 Google AI Studio API Key
GOOGLE_GENAI_API_KEY=YOUR_GOOGLE_AI_API_KEY_HERE

# 您的資料庫連線資訊
# (請注意：application.properties 中的 URL 指向一個 AWS RDS 實例，
#  請確保它是可存取的，或者將其改為您本地的 MySQL 資料庫 URL)
DB_USERNAME=YOUR_DB_USER
DB_PASSWORD=YOUR_DB_PASSWORD
```

### 2. 啟動後端 (Spring Boot)

**方法一：使用 Maven (本地)**

```bash
# 進入後端專案
cd recipe-ai-backend

# 啟動 Spring Boot 服務
./mvnw spring-boot:run
```

**方法二：使用 Docker (推薦)**

```bash
# 進入後端專案
cd recipe-ai-backend

# 1. 建置 Docker 映像檔
docker build -t recipe-ai-backend .

# 2. 運行容器，並將 .env 檔案傳入
docker run -p 8080:8080 --env-file .env recipe-ai-backend
```
後端服務將在 `http://localhost:8080` 啟動。

### 3. 啟動前端 (React)

您必須先確保後端服務正在運行。

```bash
# 進入前端專案
cd recipe-ai-frontend

# 安裝依賴
npm install

# (可選) 檢查 src/services/api.js 或 src/App.js 
# 確保 fetch URL 指向您正在運行的後端 (預設為 http://localhost:8080)

# 啟動前端開發伺服器
npm start
```
前端應用程式將在 `http://localhost:3000` 啟動。
