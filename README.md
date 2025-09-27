
# 食譜 AI 推薦系統 (Recipe AI)

## 專案簡介
本專案是一個基於 AI 的食譜推薦系統，旨在解決日常生活中「冰箱食材不知如何料理」的問題。使用者可以輸入手邊食材及飲食需求（如料理風格、飲食限制），系統將生成完整食譜，包含食材清單、步驟列表及圖片（目前為 placeholder）。  

本專案設計重點為：
- 清楚的後端 API 架構與前後端分離
- 可擴展的模組化設計
- 簡單易用的前端介面，方便快速展示 MVP

此專案目前為 MVP，暫不使用資料庫，以方便快速開發與測試核心功能。

---

## 技術架構與使用技術

### 後端
- **Java 17 + Spring Boot**
- **專案模組**
  - `RecipeController`：負責 API 路由與請求處理
  - `RecipeService`：核心邏輯處理，包括食譜生成
  - `RecipeRequest` / `RecipeResponse`：API 請求與回應封裝
- **架構特色**
  - RESTful API 設計
  - 模組化服務層，易於擴充
  - 前後端分離，後端僅提供 JSON 資料
- **工具**
  - Postman：API 測試與驗證
  - Maven：依賴管理與專案建置

### 前端
- **React**
- **功能**
  - 輸入欄位：
    - 食材（text input）
    - 料理風格/飲食需求（text input 或 select）
  - 生成食譜按鈕
  - 結果顯示區：
    - 食譜名稱
    - 食材清單
    - 步驟列表
    - 圖片區（目前 placeholder）
- **工具**
  - Node.js + npm
  - fetch API 與後端互動

---

## 目前已完成的功能
1. **後端**
   - 建立 Spring Boot 專案骨架
   - 完成 `RecipeController`、`RecipeService`、`RecipeRequest`、`RecipeResponse` 類別
   - API 路徑統一為 `/api/recipe`，並實作食譜生成邏輯
2. **前端**
   - 建立基本頁面與輸入欄位
   - 實作生成食譜按鈕，透過 fetch 呼叫後端 API
   - 顯示食譜名稱、食材列表與步驟列表
   - 圖片區使用 placeholder
3. **整合**
   - 後端 API 與前端成功串接
   - 完成 MVP 功能驗證

---

## 如何啟動與測試

### 後端
1. 克隆專案：
   ```bash
   git clone <YOUR_REPO_URL>


2. 進入專案目錄：

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
5. 使用 Postman 測試 API：

   * 範例 URL: `http://localhost:8080/api/recipe/generate`
   * POST 請求 Body JSON：

     ```json
     {
       "ingredients": "雞胸肉, 蘑菇, 洋蔥",
       "style": "中式"
     }
     ```

### 前端

1. 進入前端資料夾：

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
4. 在瀏覽器開啟 `http://localhost:3000`，輸入食材與料理風格，即可生成食譜。

---

## 未來規劃與待辦事項

1. **資料庫整合**

   * 儲存使用者歷史食譜
   * 支援食材與食譜資料管理
2. **圖片生成**

   * 接入 AI 生成食譜圖片或使用第三方 API
3. **進階推薦**

   * 加入食材替換建議
   * 根據用戶偏好調整生成結果
4. **使用者管理**

   * 註冊 / 登入功能
   * 個人化食譜收藏與管理
5. **前端優化**

   * 美化 UI / UX
   * 增加篩選與排序功能

---

**作者**
雨翰 郭

