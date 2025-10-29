package com.example.recipe_ai.service;
import com.example.recipe_ai.dto.RecipeRequest;
import com.example.recipe_ai.dto.RecipeResponse;
import com.example.recipe_ai.exception.ApiException;
import com.example.recipe_ai.service.RecipeService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apiguardian.api.API;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RecipeService 的單元測試
 * 使用 Mockito 來模擬外部依賴 (ChatModel, GeminiImageService)
 */
@ExtendWith(MockitoExtension.class) //讓我們使用mock跟injection
public class RecipeServiceTest {
    //創立假物件，要假裝輸入api去call chatmodel
    @Mock
    private ChatModel mockchatModel;
    //創立假物件，假的圖片服務生圖片
    @Mock
    private  GeminiImageService mockgeminiImageService;
    //將上面假物件注入 測試類別中
    @InjectMocks
    private  RecipeService recipeService;
    @DisplayName("成功生成食譜與圖片")
    @Test
    //成功呼叫ai api，generateRecipe呼叫buildPrompt
    //prompt呼叫 mychatModel得到json(aiResponse)，json
    //aiResponse清乾淨變成cleanAiResponse。再轉成java物價( recipeResponse)
    // 呼叫generateImage(recipeResponse)產生圖片，回傳食譜+圖片
     void test_AI_resopnse(){

        //---1.arrange(安排規劃)，假輸入
        RecipeRequest request=RecipeRequest.builder()
                .ingredients("雞肉")
                .styleOrDiet("日式")
                .build();
        //假json
        String fakeJson= """
                {
                    "title":"煎雞腿排",
                    "ingredients":["雞肉","洋蔥"],
                    "steps":["切雞肉","煎雞肉","出鍋"],
                    "imageUrl":""
                }
                """;

        //模擬chatmodel回傳的response  aiResponse = mychatModel.call(prompt).getResult().getOutput().getText();
         //不加的話測試到上面那句話，會因為沒有跟api連線掛掉
         ChatResponse mockChatResponse = mock(ChatResponse.class);
         AssistantMessage mockMessage = mock(AssistantMessage.class);
         Generation mockGeneration = mock(Generation.class);

         // **導演喊話 (when... thenReturn...)**
        // 「當(when) 假的 mychatModel 被呼叫，就回傳(thenReturn) 上面設定好的假資料(mockChatResponse)！」
         when(mockchatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
         when(mockChatResponse.getResult()).thenReturn(mockGeneration);
         when(mockGeneration.getOutput()).thenReturn(mockMessage);
         when(mockMessage.getText()).thenReturn(fakeJson);

         //模擬圖片網址(假摳gemini)，當圖片被呼叫就回傳網址
         when(mockgeminiImageService.generateImage(anyList())).thenReturn("http://fackurl-image.jpg");

        //--2.act執行---被測試的方法generateRecipe，檢查產生的recipeService是否都 正確
        RecipeResponse response= recipeService.generateRecipe(request);
        //--3.assert 驗證食譜是否被產生
         //非空
         assertNotNull(response);
         //回傳response每個值都要正確
         assertEquals("煎雞腿排",response.getTitle());
         assertEquals(Arrays.asList("雞肉","洋蔥"),response.getIngredients());
         assertEquals(Arrays.asList("切雞肉","煎雞肉","出鍋"),response.getSteps());
         assertEquals("http://fackurl-image.jpg",response.getImageUrl());
    }
    @DisplayName("食譜生成失敗，api呼叫失敗")
    @Test
    void test_generate_recipe_call_api_false()throws Exception{
        //1--arrange宣告假輸入
        RecipeRequest request= RecipeRequest.builder()
                .ingredients("雞肉")
                .styleOrDiet("日式")
                .build();
        //模擬api錯誤，當mockchatModel.call(任何 Prompt)被呼叫時出錯
        when(mockchatModel.call(any(Prompt.class)))
                .thenThrow(new RuntimeException("模擬 API 呼叫失敗!"));
        //2. act執行並驗證
        //使用 assertThrows來捕捉會出錯的recipeService.generateRecipe(request)
        ApiException exception=assertThrows(ApiException.class,()-> {
            //mychatModel.call(prompt)出錯，利用assertThrows(預期錯物,實際錯誤)
            // lambda ()-> 空參數表示法
            recipeService.generateRecipe(request);
        });
        //檢查錯誤是否跟預測相同
        //assertEquals 檢查被捕捉到的 exception 的 message 和 httpStatus 是否正確。
        assertEquals("AI 服務處理失敗，可能是API_KEY錯誤",exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,exception.getStatus());
    }
    @DisplayName("呼叫api成功，但食譜解析json失敗")
    @Test
    void test_generate_recipe_json_parse_false(){
        //1--arrange宣告假輸入，假食譜json
        RecipeRequest request= RecipeRequest.builder()
                .ingredients("雞肉")
                .styleOrDiet("日式")
                .build();
                String wrongJson= """
                {
                    "title":"煎雞腿排56515"
                    "ingredients":["雞肉","洋蔥"]
                    "steps":["切雞肉","煎雞肉","出鍋"]
                    "imageUrl";""
                }
                """;
        //宣告假chatmodel回傳
        ChatResponse mockChatResponse = mock(ChatResponse.class);
        AssistantMessage mockMessage = mock(AssistantMessage.class);
        Generation mockGeneration = mock(Generation.class);
        when(mockchatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(mockMessage);
        when(mockMessage.getText()).thenReturn(wrongJson);

        //2---執行generate recipe，當呼叫 recipeService.generateRecipe(request)
        // 是拋出錯誤會拋出「ApiException」這任何類型錯誤
        ApiException exception=assertThrows(ApiException.class, ()->{
            //內部 mapper.readValue出錯
            recipeService.generateRecipe(request);
        });
        //3 驗證 ---解析JSON失敗，異常處理，錯誤訊息是否跟預設RecipeService相同
        assertEquals("無法解析 AI 生成的食譜 JSON: ",exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,exception.getStatus());

    }
}
