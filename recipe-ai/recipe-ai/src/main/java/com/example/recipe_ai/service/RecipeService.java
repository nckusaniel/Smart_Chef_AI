package com.example.recipe_ai.service;

import com.example.recipe_ai.dto.RecipeRequest;
import com.example.recipe_ai.dto.RecipeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service        //告訴springRecipeService變成bean管理
public class RecipeService {

    //讀取application.properties的資料
    @Value("${openai.api.key}")
    private String openAiApiKey;

    public RecipeResponse generateRecipe(RecipeRequest request) {
        return  null;
    }
}
//宣告方法generateRecipe 回傳值是RecipeResponse這個類別所以會回傳/食譜名稱食材清單步驟 圖片網址
//此方法輸入類別RecipeRequest的變數request =輸入食材、飲食需求。