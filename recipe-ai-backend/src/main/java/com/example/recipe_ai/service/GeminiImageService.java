package com.example.recipe_ai.service;

import com.example.recipe_ai.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Collections;
import java.util.List;

/**
 * GeminiImageService
 * 呼叫 Google Gemini 2.5 Flash (Image Preview) API 來生成圖片。
 */
@Service
public class GeminiImageService {

    private static final String GEMINI_IMAGE_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key={apiKey}";
    //key注入到geminiAPIKEY中
    @Value("${spring.ai.google.genai.api-key:}")
    private String geminiApiKey;
    //初始化日誌 (Logger)」工具
    private static final Logger logger = LoggerFactory.getLogger(GeminiImageService.class);
    //用來發送HTTP請求
    private final RestTemplate restTemplate;
    //JAKSON函示庫，做JAVA物件<->JSON字串互相轉換
    private final ObjectMapper objectMapper;

    @Autowired
    public GeminiImageService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // 我們定義幾個「資料傳輸物件」(Data Transfer Objects, DTOs)
    // 這些 class / record 的「形狀」必須和 Google API 要求的 JSON「形狀」一致
    // `record` 是 Java 16+ 的語法，用來建立簡單的資料載體，非常方便
    //
    // 1. 最內層的 "parts" 物件： { "text": "..." }
    private static record Part(String text) {}

    // 2. "contents" 陣列中的物件： { "parts": [ { "text": "..." } ] }
    private static record Content(List<Part> parts) {}

    // 3. "generationConfig" 物件： { "responseModalities": ["IMAGE", "TEXT"] }
    private static record GenerationConfig(String[] responseModalities) {}

    // 4. 最外層的 Request "payload"： { "contents": [...], "generationConfig": {...} }
    private static record GeminiImageRequest(List<Content> contents, GenerationConfig generationConfig) {}
    // --- 新手友善的改動結束 ---


    /**
     * 呼叫 Gemini 2.5 Flash (Image Preview) 生成圖片並回傳 Base64 Data URL
     * @param steps 圖片的提示語 (料理步驟)
     * @return Base64 編碼的 Data URL (e.g., data:image/png;base64,...)
     */
    public String generateImage(List<String> steps) {

        String imagePromptText = String.format("""
                        【圖片生成指令】請使用超高清解析度、專業打光、美食特寫構圖與景深效果，
                        搭配白色陶瓷盤擺盤、木質餐桌背景、柔和自然光從左側照入，
                        呈現油亮、焦香、濕潤的食材質感，
                        根據以下食譜標題生成一張逼真的成品圖片，只輸出圖片，不要輸出任何文字說明。
                        料理步驟：%s",
                        """,
                steps
        );

        // --- 1. 建立請求 Payload (新手友善的 POJO 版本) ---
        // 這是不是比 `Map<String, Object>` 好懂 100 倍？
        // 我們在「建立物件」，而不是在「拼湊字典」
        // 建立最內層的 Part
        Part requestPart = new Part(imagePromptText);

        // 建立 Content，並把 Part 放進去
        // *** 修正 #2：使用新的變數名稱 requestPart ***
        Content content = new Content(Collections.singletonList(requestPart));

        // 建立 GenerationConfig
        GenerationConfig config = new GenerationConfig(new String[]{"IMAGE", "TEXT"});

        // 建立最外層的 Payload，把 content 和 config 放進去
        GeminiImageRequest payload = new GeminiImageRequest(
                Collections.singletonList(content),
                config
        );
        // --- POJO 版本結束 ---

        // 2. 設定 HTTP Header (這部分不變)
        // 告訴 Google：「我寄給你的資料是 JSON 格式喔」
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 3. 建立 HTTP 實體 (HttpEntity)
        // 這裡的 <GeminiImageRequest> 是關鍵！
        // 我們把「headers (信封)」和「payload (我們寫好的 Java 物件)」打包在一起
        // Spring (和它內建的 ObjectMapper) 會自動在幕後
        // 把我們的 `payload` 物件轉換成 JSON 字串！
        HttpEntity<GeminiImageRequest> entity = new HttpEntity<>(payload, headers);

        // 4. 執行 HTTP POST 請求
        String jsonResponse;
        try {
            // 這裡的 restTemplate.postForObject 解說：
            // 參數1: GEMINI_IMAGE_API_URL
            //       (我們要寄去哪裡？ 包含 {apiKey} 佔位符)
            // 參數2: entity
            //       (我們要寄什麼？ 就是上面打包好的「信封 + Java 物件」)
            // 參數3: String.class
            //       (我們預期 Google 會「回傳」什麼格式的資料？ 我們預期他回傳純文字的 JSON 字串)
            // 參數4: geminiApiKey
            //       (用來填補 URL 中 {apiKey} 佔位符的「真實變數」)
            jsonResponse = restTemplate.postForObject(
                    GEMINI_IMAGE_API_URL,
                    entity,
                    String.class,
                    geminiApiKey
            );

        } catch (Exception e) {
            logger.error("呼叫 Gemini 圖片生成服務失敗: {}", e.getMessage(), e);
            throw new ApiException("呼叫 Gemini 圖片生成服務失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 5. 解析 JSON 取得 Base64 圖片資料 (這部分不變)
        // 這裡就是你之前用 `Map` 的「反向操作」
        // 我們拿到 Google 回傳的 `jsonResponse` (純文字)
        // 然後用 `objectMapper.readTree(jsonResponse)` 把它「讀取」成
        // 方便我們用 `path()` 來取值的 `JsonNode` 物件
        //
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(jsonResponse);
        } catch (JsonProcessingException e) {
            logger.error("無法解析 Gemini 圖片回覆的 JSON: {}", jsonResponse, e);
            throw new ApiException("無法解析 Gemini 的圖片回覆 JSON", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // (解析邏輯不變)
        JsonNode partsNode = rootNode
                .path("candidates").path(0)
                .path("content").path("parts");

        String base64Image = null;
        if (partsNode.isArray()) {
            // *** 修正 #3：for 迴圈中的變數名稱從 part 改為 responsePart ***
            for (JsonNode responsePart : partsNode) {
                // *** 修正 #4：使用新的變數名稱 responsePart ***
                JsonNode inlineDataNode = responsePart.path("inlineData");
                if (!inlineDataNode.isMissingNode()) {
                    JsonNode dataNode = inlineDataNode.path("data");
                    if (dataNode.isTextual()) {
                        base64Image = dataNode.asText();
                        break;
                    }
                }
            }
        }
        if (base64Image == null) {
            logger.error("無法從 Gemini 回覆中提取 Base64 圖片資料: {}", jsonResponse);
            throw new ApiException("無法從 Gemini 回覆中提取 Base64 圖片資料。", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 6. 組合 Data URL (前端可直接使用的圖片字串)
        return "data:image/png;base64," + base64Image;
    }
}

