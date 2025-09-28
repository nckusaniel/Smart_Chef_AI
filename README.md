
# 食譜 AI 推薦系統 (Recipe AI)

## 專案簡介
本專案是一個基於 **Spring Boot + React + Spring AI** 的食譜推薦系統。  
核心目標是解決日常「冰箱有食材卻不知道怎麼料理」的問題。使用者輸入食材與飲食需求後，系統會呼叫 AI 模型，生成一份完整的食譜，包括：

- 料理名稱
- 食材清單（含數量與單位）
- 料理步驟（詳細火候、時間、器具）
- 成品圖片連結（目前使用虛擬連結）

此專案目前為 **MVP**，專注於前後端串接與 AI 輸出解析，不含資料庫，以便快速展示核心功能。

---

## 技術架構與使用技術

### 後端 (Backend)
- **語言與框架**
  - Java 17
  - Spring Boot
- **AI 整合**
  - Spring AI (`ChatModel`, `Prompt`, `UserMessage`)
  - 使用 Jackson (`ObjectMapper`) 解析 AI 回傳的 JSON
- **模組設計**
  - `RecipeController`：負責 API 路由與請求處理 (`/api/recipe/generate`)
  - `RecipeService`：核心邏輯，呼叫 AI 模型並解析 JSON
  - `RecipeRequest`：使用者請求 DTO
  - `RecipeResponse`：AI 回傳結果 DTO
- **錯誤處理**
  - AI 回傳 JSON 若解析失敗，會提供一組預設食譜（fallback）

### 前端 (Frontend)
- **語言與框架**
  - React
- **主要功能**
  - 表單輸入：食材、料理風格/飲食需求
  - 呼叫後端 API 產生食譜
  - 結果顯示區：料理名稱、食材清單、步驟、圖片（placeholder）

### 開發工具
- Postman：API 測試
- Maven：專案建置與依賴管理
- Node.js + npm：前端建置與套件管理

---

## 目前已完成的功能
1. **後端**
   - Spring Boot 專案骨架完成
   - API 路由統一為 `/api/recipe`
   - RecipeService 整合 Spring AI，生成 JSON 食譜並轉換為 `RecipeResponse`
   - JSON 解析錯誤時提供 fallback 機制
2. **前端**
   - 表單輸入與 API 串接
   - 結果區顯示：名稱 / 食材 / 步驟 / 圖片 placeholder
3. **整合**
   - 前後端串接成功
   - 使用者可輸入需求並即時獲取食譜

---

## 如何啟動與測試

### 後端
1. 克隆專案：
   ```bash
   git clone <YOUR_REPO_URL>
    ````

2. 進入專案資料夾：

   ```bash
   cd recipe-ai
   ```
3. 使用 Maven 建置：

   ```bash
   mvn clean install
   ```
4. 啟動 Spring Boot：

   ```bash
   mvn spring-boot:run
   ```
5. 測試 API（以 Postman 或 curl 測試）：

   * URL: `http://localhost:8080/api/recipe/generate`
   * 方法：`POST`
   * Body 範例：

     ```json
     {
       "ingredients": "雞胸肉, 洋蔥, 蘑菇",
       "styleOrDiet": "中式"
     }
     ```

### 前端

1. 進入前端目錄：

   ```bash
   cd frontend
   ```
2. 安裝依賴：

   ```bash
   npm install
   ```
3. 啟動開發伺服器：

   ```bash
   npm start
   ```
4. 瀏覽器開啟 `http://localhost:3000`，即可操作。

---

## 未來規劃與待辦事項

* **資料庫整合**

  * 儲存歷史查詢結果與使用者偏好
* **圖片生成**

  * 整合 AI 圖片 API，自動生成料理成品圖片
* **使用者功能**

  * 註冊 / 登入 / 收藏食譜
  * 個人化推薦
* **前端優化**

  * UI 美化、步驟展示動畫化
  * 食材搜尋建議
* **進階推薦邏輯**

  * 自動建議替代食材
  * 飲食限制（低碳、素食等）自動篩選

---
![image](https://hackmd.io/_uploads/HylQV3L2xe.png)

## 作者

郭雨翰

此專案設計重點在於 **前後端分離 + AI JSON 輸出解析 + 清晰的模組化架構**，方便日後擴展至完整的 SaaS 或商業應用。

