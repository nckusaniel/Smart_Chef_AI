package com.example.recipe_ai.service;

import com.example.recipe_ai.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GeminiImageService
 * 專門負責呼叫 Google Gemini 2.5 Flash (Image Preview) API 來生成圖片。
 * 這是使用免費額度模型的方法。
 */
@Service
public class GeminiImageService {

    // 指定模型--- gemini-2.5-flash-image-preview---
    private static final String GEMINI_IMAGE_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key={apiKey}";

    // value--從 application.properties  讀取spring.ai.google.genai.api-key，geminiApiKey
    @Value("${spring.ai.google.genai.api-key:}")
    private String geminiApiKey;

    // 加入 Logger
    private static final Logger logger = LoggerFactory.getLogger(GeminiImageService.class);
    // new 兩個物件，分別是 RestTemplate 和 ObjectMapper：
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * 呼叫 Gemini 2.5 Flash (Image Preview) 生成圖片並回傳 Base64 Data URL
     * @param steps 圖片的提示語 (料理步驟)
     * @return Base64 編碼的 Data URL (e.g., data:image/png;base64,...)
     */
    public String generateImage(List<String> steps) {
        // 用料理步驟作為prompt
        String imagePromptText = String.format("""
                        【圖片生成指令】請使用超高清解析度、專業打光、美食特寫構圖與景深效果，
                        搭配白色陶瓷盤擺盤、木質餐桌背景、柔和自然光從左側照入，
                        呈現油亮、焦香、濕潤的食材質感，
                        根據以下食譜標題生成一張逼真的成品圖片，只輸出圖片，不要輸出任何文字說明。
                        料理步驟：%s",
                        """,
                steps
        );
        // 1. 建立傳給 gemini的JSON 格式，API  有嚴格的參數名稱規範。
        // --- 組裝 Contents ---  API指定content包含 part包含text包含prompt
        Map<String, Object> parts = Collections.singletonMap("text", imagePromptText);
        Map<String, Object> contents = Collections.singletonMap("parts", Collections.singletonList(parts));
        //contents
        // {
        //  "parts": [
        //    { "text": "imagePromptText" }
        //  ]
        //}

        // API指定 generationConfig  key=responseModalities(回傳型態)  value=型態 [圖片、文字]
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("responseModalities", new String[]{"IMAGE", "TEXT"});

        // --- 最終 json取名為payload。有兩筆資料
        // contents:傳給ai的內容跟、 generationConfig AI 模型生成的內容配置 ---
        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", Collections.singletonList(contents));
        payload.put("generationConfig", generationConfig);
//        payload
//         {
//          "contents": [
//            {
//              "parts": [
//                { "text": "一隻可愛的小貓坐在沙發上" }
//              ]
//            }
//          ],
//          "generationConfig": {
//            "responseModalities": ["IMAGE", "TEXT"]
//          }
//        }

        // 2. 設定 HTTP Header
        HttpHeaders headers = new HttpHeaders();        //建立 HTTP Header 物件，用來告訴伺服器「我要傳什麼格式的資料」。
        headers.setContentType(MediaType.APPLICATION_JSON);     //告訴伺服器：傳的是 JSON 格式的資料
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        //把payload, headers包在一起傳出，payload--傳出的內容、header---傳出的格式

        // 3. 執行 HTTP POST 請求
        String jsonResponse;        //存放gemini回應
        try {
            //restTemplate.postForObject發送post到網路上
            jsonResponse = restTemplate.postForObject(
                    GEMINI_IMAGE_API_URL,       //我發送post的 api網址
                    entity,                     //傳給gemini ai  api的資料
                    String.class,               //api回傳內容後，字串存放
                    geminiApiKey                // 替換 URL 中的 {apiKey}
            );
        } catch (Exception e) {
            logger.error("呼叫 Gemini 圖片生成服務失敗: {}", e.getMessage(), e);
            throw new ApiException("呼叫 Gemini 圖片生成服務失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        //jsonResponse內容
        // -> candidates[0]
        //     -> content
        //         -> parts (這是一個陣列 [...])
        //             -> [陣列中的某一個元素，例如 index 1]
        //                 -> inlineData
        //                     -> data (這裡才是圖片 Base64 資料)

        // 4. 解析 JSON 取得 Base64 圖片資料
        JsonNode rootNode;
        try {
            //  4.1 readTree會把jsonResponse解析成樹狀結構的jsonnode，存放到rootNode。
            rootNode = objectMapper.readTree(jsonResponse);

        }
        catch (JsonProcessingException e) {
            logger.error("無法解析 Gemini 圖片回覆的 JSON: {}", jsonResponse, e);
            throw new ApiException("無法解析 Gemini 的圖片回覆 JSON", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 4.2 取得 candidates 中的content 中的 parts _arr
        JsonNode partsNode = rootNode
                .path("candidates").path(0)
                .path("content").path("parts");

        String base64Image = null;

        //4.3找jsonnode中的base64data。
        if (partsNode.isArray()) {
            // 用for-each，檢查partsNode中每個parts 陣列，尋找包含 inlineData (圖片) 的節點
            for (JsonNode part : partsNode) {
                //取出part.path 中 "inlineData" 欄位
                JsonNode inlineDataNode = part.path("inlineData");
                // 檢查inlineData是否有資料
                if (!inlineDataNode.isMissingNode()) {
                    //取出 inlineDataNode中的 'data'欄位 (Base64 字串)
                    JsonNode dataNode = inlineDataNode.path("data");
                    // 檢查 data 是否存在且為文字 (即 Base64 字串)
                    if (dataNode.isTextual()) {
                        //將dataNode賦予給base64Image變數
                        base64Image = dataNode.asText();
                        // 找到圖片後跳出迴圈
                        break;
                    }
                }
            }
        }
        // 如果沒有找到 Base64 圖片資料，拋出異常
        if (base64Image == null) {
            logger.error("無法從 Gemini 回覆中提取 Base64 圖片資料: {}", jsonResponse);
            throw new ApiException("無法從 Gemini 回覆中提取 Base64 圖片資料。", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // 5. 組合 Data URL (前端可直接使用的圖片字串)
        return "data:image/png;base64," + base64Image;
    }
}
//json
// {
//  "candidates": [
//    {
//      "content": {
//        "parts": [
//          { "text": "Hello!" },
//          {
//            "inlineData": {
//              "mimeType": "image/png",
//              "data": "iVBORw0KGgoAAAANSUhEUgAAAAUA..."  // Base64圖片
//            }
//          }
//        ]
//      }
//    }
//  ]
//}