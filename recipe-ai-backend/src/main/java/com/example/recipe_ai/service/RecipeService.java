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
//資料庫相關
import com.example.recipe_ai.entity.RecipeCache;
import com.example.recipe_ai.repository.RecipeCacheRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
/**
 * RecipeService
 * 核心邏輯：呼叫 Spring AI 的 ChatModel 產生食譜文字，並呼叫 GeminiImageService 產生圖片 Data
 */
@Service // 告訴 Spring 這是一個服務類別，會被自動管理（變成 Bean）
public class RecipeService {

    //宣告LOGGER
    private static final Logger logger=LoggerFactory.getLogger(RecipeService.class);
    // 宣告ChatModel型態的變數mychatModel，用來跟 AI 模型互動（用於生成食譜文字/JSON）
    private final ChatModel mychatModel;
    // 宣告GeminiImageService型態的變數mygeminiImageService，用來呼叫GeminiImageService中的方法
    private final GeminiImageService mygeminiImageService;
    // 宣告ObjectMapper型態變數mapper，負責把json轉換成java，或java轉換成json
    private final ObjectMapper mapper = new ObjectMapper();
    //宣告recipeCacheRepository，來跟資料庫互動
    private final RecipeCacheRepository myrecipeCacheRepository;

    // 建構子注入:Spring 自動把ChatModel、 GeminiImageService 物件（Bean）們當作參數傳進來，讓我賦予變數值。
    public RecipeService(ChatModel chatModel, GeminiImageService geminiImageService,RecipeCacheRepository recipeCacheRepository) {
        this.mychatModel = chatModel;
        this.mygeminiImageService = geminiImageService;
        this.myrecipeCacheRepository=recipeCacheRepository;
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
     * 核心邏輯：根據輸入，檢查是否存在於資料庫，有--直接回傳，沒有--呼叫 AI 模型生成食譜，接著呼叫 Gemini 生成圖片，存入資料庫
     */
    public RecipeResponse generateRecipe(RecipeRequest request) {
        //----判斷需求是否存在資料庫
        //1.產生此需求的key
        String key= generate_key(request);
        System.out.println("log:正在查詢key:"+key);

        //2.檢查key是否在資料庫，找到回查那筆資料，找不對回傳null，用Optional<RecipeCache>接收
        //有資料 → Optional 內部就存了一個 RecipeCache 物件
        //沒資料 → Optional 是空的
        Optional<RecipeCache> search_result=myrecipeCacheRepository.findById(key);
        //3. Optional非空，資料庫有這筆key
        if(search_result.isPresent()){
            System.out.println("log:資料庫找到食譜資料");//資料庫資料，放入old_data，之後做存取
            RecipeCache old_data=search_result.get();

            //4.將old_data放入recipeResponse回傳
            RecipeResponse recipeResponse = new RecipeResponse();

            //先處理資料庫跟dto都是string的部分
            recipeResponse.setTitle(old_data.getTitle());
            recipeResponse.setImageUrl(old_data.getImageUrl());

            // DTO中 recipeResponse的 ingredients、steps必須是 list<string>，但資料庫中是string
            // 用 Arrays.asList 和 split，來將string還原成 List<string>。資料庫中用 ||來分隔每個不同元素

            // recipeResponse放入Ingredients
            if (old_data.getIngredients() != null && !old_data.getIngredients().isEmpty()) {
                // 使用 split("\\|\\|")因為|要用 \\來跳脫，兩個||就是 \\ | || |
                recipeResponse.setIngredients(Arrays.asList(old_data.getIngredients().split("\\|\\|")));
            } else {
                recipeResponse.setIngredients(Collections.emptyList());
            }

            // recipeResponse放入steps
            if (old_data.getSteps() != null && !old_data.getSteps().isEmpty()) {
                recipeResponse.setSteps(Arrays.asList(old_data.getSteps().split("\\|\\|")));
            } else {
                recipeResponse.setSteps(Collections.emptyList());
            }
            //回傳給前端
            return  recipeResponse;
        }

        System.out.println("LOG: 快取錯失 (Miss)! 準備呼叫 AI...");

        //5. -------資料庫沒有key-----呼叫ai產生食譜
        String aiResponse;
        try {
            // 5.1 產生prompt，並呼叫gemini的chatModel
            Prompt prompt = buildPrompt(request);

            //將gemini的回應取出，存入airesponse。  aiResponse是json格式，因為prompt指定
            aiResponse = mychatModel.call(prompt).getResult().getOutput().getText();

        } catch (Exception e) {
            //----模型呼叫失敗---
            logger.error("呼叫 Gemini AI 模型失敗: " + e.getMessage(), e); // 1.把錯誤印在後台日誌除錯

            // 2. 回傳一個更通用的錯誤訊息給前端
            throw new ApiException("AI 服務處理失敗，可能是API_KEY錯誤", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // 5.2 將 JSON 字串轉成 RecipeResponse 物件格式
        RecipeResponse recipeResponse;
        try {
            //清理aiResponse，避免轉換錯誤
            String cleanAiResponse = aiResponse.replace("```json", "").replace("```", "").trim();

            //mapper.readValue(String content, Class<T> valueType)
            //content：要解析的 JSON 字串、 valueType：希望生成的 Java 類別
            recipeResponse = mapper.readValue(cleanAiResponse, RecipeResponse.class);
            //mapper是jakson轉換器--->readvalue來說明: 誰 轉換成 和型態java物件

        } catch (JsonProcessingException e) {
            // JSON 轉換失敗
            logger.error("無法解析 AI 回傳的 JSON: " + aiResponse, e);
            throw new ApiException("無法解析 AI 生成的食譜 JSON: " , HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 5.3. 使用食譜步驟作為圖片prompt，呼叫 Gemini 生成圖片
        //呼叫圖片生成
        String imageUrl = mygeminiImageService.generateImage(recipeResponse.getSteps());

        // 5.4. recipeResponse 中加入imageUrl
        recipeResponse.setImageUrl(imageUrl);

        //5.5 儲存 進資料庫
        System.out.println("LOG: 正在將 AI 結果存入資料庫...");

        RecipeCache new_cache_entry=new RecipeCache();
        //將recipeResponse的資料放入 new_cache_entry

        new_cache_entry.setKey_id(key);                             //放入key
        new_cache_entry.setTitle(recipeResponse.getTitle());        //放入料理標題
        new_cache_entry.setImageUrl(recipeResponse.getImageUrl());  //放入圖片

        // recipeResponse 中ingredients、steps 型態為List<String>。而database只能存放list，所以要List<String>轉換成String
        //String.join: 把一個 List多個字串元素「用指定分隔符」連起來，變成一條字串。 List<String--->String
        new_cache_entry.setIngredients(String.join("||", recipeResponse.getIngredients()));
        new_cache_entry.setSteps(String.join("||", recipeResponse.getSteps()));
        //["雞胸肉 200g", "洋蔥 半顆", "橄欖油 1 湯匙"] ---->"雞胸肉 200g||洋蔥 半顆||橄欖油 1 湯匙"

        // 存入資料庫
        myrecipeCacheRepository.save(new_cache_entry);
        return recipeResponse;
    }

    //-----產生key函數-----
    private String generate_key(RecipeRequest request){
        //輸入資料正規化
        String normalize_ingredients= normalizeString(request.getIngredients());
        String normalize_style=normalizeString(request.getStyleOrDiet());
        //正規化資料組合成key
        String key=normalize_ingredients+"::"+normalize_style;
        return key;
    }
    //正規化---1.檢查空字串 、 2.字串切割 、 3.轉小寫、 4.去除每個字串的頭尾空白、 5.按開頭排序字串陣列、 6.用|重組字串
    // string --切割成---string [] ----處理完後---組合成 string (用|間隔不同元素)
    private String normalizeString(String input){
        //1. 輸入是空的，回傳空字串
        if(input == null || input.trim().isEmpty()){
            return  "";
        }
        //2. 切割字串split，因為input是"雞肉, 洋蔥"string。所以要先切割成多個字串，才可以進一步處理
        // 遇到特殊字元，就分割成多個字串陣列(代表不同食材
        String [] string_arr = input.split("[,，、]"); // 只用這三種符號分割
        //清理 map(代表對每個字串轉換
        String normalize_string=Arrays.stream(string_arr)
                .map(String::toLowerCase)   //3.轉小寫
                .map(String::trim)          //4.刪除每個陣列，頭尾空白
                .sorted()                   //5.每個陣列按開頭排序
                .collect(Collectors.joining("|"));  //把 Stream 集成最終結果，用|分隔，輸出預設string
        return  normalize_string;
    }



}
