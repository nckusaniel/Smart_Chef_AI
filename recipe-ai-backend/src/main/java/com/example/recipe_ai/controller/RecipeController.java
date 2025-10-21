package com.example.recipe_ai.controller;

import com.example.recipe_ai.dto.RecipeRequest;
import com.example.recipe_ai.dto.RecipeResponse;
import com.example.recipe_ai.service.RecipeService;
import org.springframework.web.bind.annotation.*;

// 標記這個類別 (Class) 是一個「API 控制器」，接收前端的網路請求，並「回傳 JSON 資料」@controller+ @ResponseBody。
@RestController
// 允許「所有來源」的網址，都可以來呼叫這裡的 API。解決 CORS 跨域問題。
@CrossOrigin(origins = "*")
// 統一此class控制器裡所有 API 的「路徑」，都以 "/api/recipe" 開頭
@RequestMapping("/api/recipe")

/**
 * Recipe Controller負責處理前端關於食譜的 HTTP 請求，並呼叫後端的食譜生成服務。
 */
public class RecipeController {

    // 宣告my_recipeService變數，為了讓下面建構子，可以注入實例
    private final RecipeService myrecipeService;

    /**
     * RecipeController 的建構子。
     * Spring 框架會自動透過此建構子注入 (DI) 一個 RecipeService 的實例。
     * @param recipeService 由 Spring 容器提供的食譜服務實例
     */
    public RecipeController(RecipeService recipeService) {

        this.myrecipeService = recipeService;     //附值給自己
    }

    /**
     * 這是一個產生食譜 API，名為handle_generateRecipe
     * @param request - 顧客遞給你的「點單」 (RecipeRequest)
     * @return RecipeResponse - 你端回給顧客的「完成的菜餚」 (RecipeResponse)
     */
    @PostMapping("/generate")
    public RecipeResponse handle_generateRecipe(@RequestBody RecipeRequest request) {
        return myrecipeService.generateRecipe(request);
    }
}

// 你可以在這裡加入更多 API (例如：查詢食譜、刪除食譜...)

//private final RecipeService my_recipeService，宣告變數my_recipeService，為了使用RecipeService類別的generateRecipe。
//public RecipeController(RecipeService recipeService) {this.my_recipeService = recipeService;//變數實例化，讓我等等可用RecipeService的方法
//public RecipeResponse handle_generateRecipe(@RequestBody RecipeRequest request)
//宣告方法來連接前後端，輸入RecipeRequest request 利用my_recipeService呼叫方法generateRecipe(request); 並回傳 RecipeResponse型態給前端