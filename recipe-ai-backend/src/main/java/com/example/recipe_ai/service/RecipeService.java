package com.example.recipe_ai.service;
//內建DTO呼叫
import com.example.recipe_ai.dto.RecipeRequest;
import com.example.recipe_ai.dto.RecipeResponse;
//呼叫SPRING AI
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
//使用@Service
import org.springframework.stereotype.Service;

//Jackson 相關類別
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Arrays;

/**
 * RecipeService
 * 核心邏輯：呼叫 Spring AI 的 ChatModel 產生食譜文字，並呼叫 GeminiImageService 產生圖片 Data
 */
@Service // 告訴 Spring 這是一個服務類別，會被自動管理（變成 Bean）
public class RecipeService {

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
        // 1. 生成食譜文字（JSON）
        Prompt prompt = buildPrompt(request);
        String aiResponse = mychatModel.call(prompt).getResult().getOutput().getText() ;
        System.out.println("AI 食譜 JSON 回覆內容：" + aiResponse);

        // 2. 將 JSON 字串轉成 RecipeResponse 物件
        RecipeResponse recipeResponse;
        try {
            // 清理可能出現的 Markdown 符號（例如 LLM 誤回傳的 ```json ... ```）
            String cleanAiResponse = aiResponse.replace("```json", "").replace("```", "").trim();
            recipeResponse = mapper.readValue(cleanAiResponse, RecipeResponse.class);
        } catch (JsonProcessingException e) {
            System.err.println("解析食譜 JSON 失敗：" + e.getMessage());
            // 如果解析失敗，使用一個包含錯誤訊息的連結回傳預設值
            return RecipeResponse.builder()
                    .title("食譜生成失敗 (JSON Error)")
                    .ingredients(Arrays.asList("請檢查 AI 回覆格式"))
                    .steps(Arrays.asList("步驟無法顯示"))
                    .imageUrl("https://via.placeholder.com/300?text=JSON+Parse+Error") // 改用實際的 URL，而不是 Markdown
                    .build();
        }

        // 3. 使用食譜標題作為圖片提示語，呼叫 Gemini 生成圖片
        try {
            String imageUrl = mygeminiImageService.generateImage(recipeResponse.getSteps());
            //4. 更新 Response 物件中的 imageUrl
            recipeResponse.setImageUrl(imageUrl);
        } catch (Exception e) {
            System.err.println("Gemini 圖片生成失敗：" + e.getMessage());
            // 如果圖片生成失敗，回傳一個錯誤圖片連結
            recipeResponse.setImageUrl("https://via.placeholder.com/300?text=Image+Generation+Error");
        }

        return recipeResponse;
    }
}