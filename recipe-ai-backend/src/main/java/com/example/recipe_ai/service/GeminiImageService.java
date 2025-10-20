package com.example.recipe_ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
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

    // 使用 gemini-2.5-flash-image-preview 模型和 generateContent 端點
    private static final String GEMINI_IMAGE_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image-preview:generateContent?key={apiKey}";

    // 從 application.properties 或 application.yml 讀取 API Key
    // 確保您的設定檔中配置了 gemini.api.key
    @Value("${spring.ai.google.genai.api-key:}")
    private String geminiApiKey;

    // 實例化 RestTemplate 和 ObjectMapper
    // 注意：在實際 Spring 專案中，RestTemplate 建議透過 @Bean 配置
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 呼叫 Gemini 2.5 Flash (Image Preview) 生成圖片並回傳 Base64 Data URL
     * @param steps 圖片的提示語 (料理步驟)
     * @return Base64 編碼的 Data URL (e.g., data:image/png;base64,...)
     * @throws Exception 如果 API 呼叫或解析失敗
     */
    public String generateImage(List<String> steps) throws Exception {
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            // 如果沒有 API Key，回傳佔位符
            System.err.println("警告：Gemini API Key 未配置，無法呼叫圖片生成服務。");
            return "https://via.placeholder.com/300?text=NO+API+KEY";
        }

        // 1. 建立請求 Payload (JSON 格式)
        // 提示語中要求模型生成圖片，以料理步驟作為主題
        String imagePromptText = String.format(
                "【圖片生成指令】請使用超高清解析度、專業打光、美食特寫構圖與景深效果，搭配白色陶瓷盤擺盤、木質餐桌背景、柔和自然光從左側照入，呈現油亮、焦香、濕潤的食材質感，根據以下食譜標題生成一張逼真的成品圖片，只輸出圖片，不要輸出任何文字說明。料理步驟：「%s」。",
                steps
        );

        // --- 組裝 Contents ---
        Map<String, Object> parts = Collections.singletonMap("text", imagePromptText);
        Map<String, Object> contents = Collections.singletonMap("parts", Collections.singletonList(parts));

        // --- 組裝 generationConfig (關鍵：設定 responseModalities 為 IMAGE 和 TEXT) ---
        Map<String, Object> generationConfig = new HashMap<>();
        // 這是關鍵：要求模型回傳 IMAGE
        generationConfig.put("responseModalities", new String[]{"IMAGE", "TEXT"});

        // --- 最終 Payload ---
        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", Collections.singletonList(contents));
        payload.put("generationConfig", generationConfig);

        // 2. 設定 HTTP Header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        // 3. 執行 HTTP POST 請求
        String jsonResponse;
        try {
            // restTemplate.postForObject 會將 {apiKey} 替換為 geminiApiKey
            jsonResponse = restTemplate.postForObject(
                    GEMINI_IMAGE_API_URL,
                    entity,
                    String.class,
                    geminiApiKey    // 傳入參數，替換 URL 中的 {apiKey}
            );
        } catch (Exception e) {
            // 處理網路或 API 錯誤
            System.err.println("Gemini API 呼叫失敗: " + e.getMessage());
            throw new RuntimeException("無法連線至 Gemini 圖片生成服務", e);
        }

        // 4. 解析 JSON 取得 Base64 圖片資料 (修正後的強健解析邏輯)
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // 取得 candidates -> 0 -> content -> parts 陣列
        JsonNode partsNode = rootNode
                .path("candidates").path(0)
                .path("content").path("parts");

        String base64Image = null;

        if (partsNode.isArray()) {
            // 迭代 parts 陣列，尋找包含 inlineData (圖片) 的節點
            for (JsonNode part : partsNode) {
                JsonNode inlineDataNode = part.path("inlineData");

                // 檢查是否存在 inlineData 節點
                if (!inlineDataNode.isMissingNode()) {
                    JsonNode dataNode = inlineDataNode.path("data");

                    // 檢查 data 節點是否存在且為文字 (即 Base64 字串)
                    if (dataNode.isTextual()) {
                        base64Image = dataNode.asText();
                        // 找到圖片後立即跳出迴圈
                        break;
                    }
                }
            }
        }

        // 如果沒有找到 Base64 圖片資料，拋出異常
        if (base64Image == null) {
            System.err.println("解析 Base64 圖片資料失敗，JSON 結構不符或模型生成圖片失敗。");
            System.err.println("原始回覆：\n" + jsonResponse);
            throw new RuntimeException("無法從 Gemini 回覆中提取 Base64 圖片資料。");
        }

        // 5. 組合 Data URL (前端可直接使用的圖片字串)
        return "data:image/png;base64," + base64Image;
    }
}