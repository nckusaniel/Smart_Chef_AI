package com.example.recipe_ai.service;

import com.example.recipe_ai.dto.RecipeRequest;   // 使用者輸入的資料格式（食材、料理需求）
import com.example.recipe_ai.dto.RecipeResponse;  // AI 回傳的食譜格式（標題、食材、步驟、圖片）
import org.springframework.ai.chat.model.ChatModel;   // Spring AI 提供的聊天模型介面
import org.springframework.ai.chat.messages.UserMessage; // 使用者訊息物件，用來包裝我們的提問
import org.springframework.ai.chat.prompt.Prompt;       // Prompt 是 AI 的輸入格式
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Arrays;
import java.util.List;

/**
 * RecipeService
 * 核心邏輯：呼叫 Spring AI 的 ChatModel 產生食譜
 */
@Service  // 告訴 Spring 這是一個服務類別，會被自動管理（變成 Bean）
public class RecipeService {

    // 宣告一個 ChatModel 物件，用來跟 AI 模型互動
    private final ChatModel chatModel;

    // 建構子注入：Spring 會自動幫我們把 chatModel 傳進來
    @Autowired
    public RecipeService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 建立 Prompt，告訴 AI 要怎麼輸出 JSON 食譜格式
     * @param request 使用者輸入的食材與料理風格
     * @return Prompt 物件，給 AI 模型使用
     */
    private Prompt buildPrompt(RecipeRequest request) {
        String promptText = String.format(
                "請根據以下輸入，生成詳細且專業的食譜，請只回傳純 JSON 格式，不要加任何說明文字或 Markdown 格式（例如 ```json）。\n\n" +
                        "請根據以下輸入，回傳純 JSON 格式的食譜，不要加任何說明文字或 Markdown 格式（例如 ```json）。\n\n" +
                        "請務必讓食譜內容非常詳細，包括：\n" +
                        "1. 食材請列出完整名稱與數量（例如：雞胸肉 200g、洋蔥 半顆）\n" +
                        "2. 步驟請具體描述每個動作、時間、火候、器具（例如：用中火加熱平底鍋 2 分鐘，加入橄欖油 1 湯匙）\n" +
                        "3. 請確保步驟邏輯清晰，能讓初學者照著完成料理\n" +
                        "4. 請補上合理的 imageUrl（可用虛擬連結）\n\n" +
                        "食材: %s\n" +
                        "料理需求: %s\n\n" +
                        "輸出格式範例：\n" +
                        "{\n" +
                        "  \"title\": \"料理名稱\",\n" +
                        "  \"ingredients\": [\"雞胸肉 200g\", \"洋蔥 半顆\"],\n" +
                        "  \"steps\": [\"步驟1：用中火加熱平底鍋 2 分鐘，加入橄欖油 1 湯匙。\", \"步驟2：放入雞胸肉煎至兩面金黃，每面約 3 分鐘。\"],\n" +
                        "  \"imageUrl\": \"https://example.com/image.jpg\"\n" +
                        "}",
                request.getIngredients(),       // 使用者輸入的食材
                request.getStyleOrDiet()        // 使用者輸入的料理風格或飲食限制
        );

        return new Prompt(new UserMessage(promptText));     // 把文字包裝成 UserMessage，再轉成 Prompt 給 AI 模型使用，因為- Spring AI 的 ChatClient.call(...) 只接受 Prompt 作為輸入
    }

    /**
     * 核根據輸入，呼叫 AI 模型生成食譜
     * @param request 使用者輸入的食材與料理需求
     * @return RecipeResponse 包含食譜標題、食材清單、步驟、圖片網址
     */
    public RecipeResponse generateRecipe(RecipeRequest request) {
        // 建立提示語（Prompt）
        Prompt prompt = buildPrompt(request);

        // 呼叫 AI 模型，取得回覆文字（新版 Spring AI 用 getText()）
        String aiResponse = chatModel
                .call(prompt)   //把 prompt 丟給 AI 模型
                .getResult()    // 拿到 AI 的回覆結果物件
                .getOutput()    //拿到 AI 的輸出內容
                .getText();     //拿到 AI 回覆的純文字（根據 promptText是JSON 格式的食譜）

        // 🧪 加這行 log，方便你看到 AI 回了什麼
        System.out.println("AI 回覆內容：" + aiResponse);

        // 使用 Jackson 將 JSON 字串轉成 RecipeResponse 物件
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(aiResponse, RecipeResponse.class);
        } catch (JsonProcessingException e) {
            System.err.println("解析失敗：" + e.getMessage());
            return RecipeResponse.builder()
                    .title("預設食譜")
                    .ingredients(Arrays.asList("雞肉", "洋蔥"))
                    .steps(Arrays.asList("切雞肉", "炒洋蔥"))
                    .imageUrl("https://via.placeholder.com/300")
                    .build();
        }
    }
}