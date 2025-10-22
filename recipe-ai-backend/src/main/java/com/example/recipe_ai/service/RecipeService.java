package com.example.recipe_ai.service;
//內建DTO呼叫
import com.example.recipe_ai.dto.RecipeRequest;
import com.example.recipe_ai.dto.RecipeResponse;
//呼叫SPRING AI
import com.example.recipe_ai.exception.ApiException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
//使用@Service
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
//Jackson 相關類別
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
//輸入LOGGER
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RecipeService
 * 核心邏輯：呼叫 Spring AI 的 ChatModel 產生食譜文字，並呼叫 GeminiImageService 產生圖片 Data
 */
@Service // 告訴 Spring 這是一個服務類別，會被自動管理（變成 Bean）
public class RecipeService {

    //宣告LOGGER
    private static  Logger logger=LoggerFactory.getLogger(RecipeService.class);
    // 宣告ChatModel型態變數mychatModel，用來跟 AI 模型互動（用於生成食譜文字/JSON）
    private final ChatModel mychatModel;

    // 宣告GeminiImageService 型態變數mygeminiImageService，
    private final GeminiImageService mygeminiImageService;
    // 宣告ObjectMapper型態變數mapper
    private final ObjectMapper mapper = new ObjectMapper();

    // 建構子注入:Spring 自動把ChatModel、 GeminiImageService 物件（Bean）們當作參數傳進來，讓我賦予變數值。
    public RecipeService(ChatModel chatModel, GeminiImageService geminiImageService) {
        this.mychatModel = chatModel;
        this.mygeminiImageService = geminiImageService;
    }

    /**
     * 建立 Prompt，告訴 AI 要怎麼輸出 JSON 食譜格式
     */
    private Prompt buildPrompt(RecipeRequest request) {
        //宣告字串變數promptText，存放給ai的指令
        String promptText = String.format("""
                請根據以下輸入，回傳**純 JSON 格式**的食譜，**不要加任何說明文字或 Markdown 格式**（例如 ```json ... ```）。
                請務必讓食譜內容非常詳細，包括：
                1. 食材請列出完整名稱與數量（例如：雞胸肉 200g、洋蔥 半顆）
                2. 步驟請具體描述每個動作、時間、火候、器具（例如：用中火加熱平底鍋 2 分鐘，加入橄欖油 1 湯匙）
                3. 請確保步驟邏輯清晰，能讓初學者照著完成料理
                4. imageUrl 欄位請留空，因為圖片將透過另一個服務生成。

                **食材**：%s
                **料理需求**：%s

                **請嚴格遵循以下 JSON 格式輸出**：
                {
                  "title": "料理名稱",
                  "ingredients": ["食材1", "食材2", "...", "..."],
                  "steps": ["步驟1", "步驟2", "...", "..."],
                  "imageUrl": ""
                }
                """,
                request.getIngredients(),
                request.getStyleOrDiet()
        );

        return new Prompt(new UserMessage(promptText));
    }

    /**
     * 核心邏輯：根據輸入，呼叫 AI 模型生成食譜，接著呼叫 Gemini 生成圖片。
     */
    public RecipeResponse generateRecipe(RecipeRequest request) {
        String aiResponse;
        try {
            // 1. 生成食譜文字（JSON）
            Prompt prompt = buildPrompt(request);
            aiResponse = mychatModel.call(prompt).getResult().getOutput().getText();

        } catch (Exception e) {
            // 1.把錯誤印在後台日誌，才能除錯
            logger.error("呼叫 Gemini AI 模型失敗: " + e.getMessage(), e);
            // 2. 回傳一個更通用的錯誤訊息給前端
            // 500 Internal Server Error 是一個更適合的 "catch-all" 狀態
            throw new ApiException("AI 服務處理失敗，可能是API_KEY錯誤", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // 2. 將 JSON 字串轉成 RecipeResponse 物件
        RecipeResponse recipeResponse;
        try {
            String cleanAiResponse = aiResponse.replace("```json", "").replace("```", "").trim();
            recipeResponse = mapper.readValue(cleanAiResponse, RecipeResponse.class);
        } catch (JsonProcessingException e) {
            // JSON 解析失敗也Log
            logger.error("無法解析 AI 回傳的 JSON: " + aiResponse, e);
            throw new ApiException("無法解析 AI 生成的食譜 JSON: " , HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 3. 使用食譜步驟作為圖片提示語，呼叫 Gemini 生成圖片
        String imageUrl = mygeminiImageService.generateImage(recipeResponse.getSteps());

        // 4. 更新 Response 物件中的 imageUrl
        recipeResponse.setImageUrl(imageUrl);

        return recipeResponse;
    }
}
