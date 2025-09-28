package com.example.recipe_ai.controller;
import com.example.recipe_ai.dto.RecipeRequest;
import com.example.recipe_ai.dto.RecipeResponse;
import com.example.recipe_ai.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * RecipeController
 * 提供 API 給前端呼叫
 */
@RestController     //告訴SPRING BOOT 這個類別是一個(restfulapi)的控制器，主要處理前端輸入的請求。
@CrossOrigin(origins = "*")     //react網址通用
@RequestMapping("/api/recipe")  //告訴spring boot，此類別中所有api要使用，都要連線到http...api/recipe

/*
 * 處理與食譜相關的 HTTP 請求的控制器 (Controller)。
 * * 這個類別負責接收客戶端的請求，並透過 RecipeService 來處理業務邏輯。
 */
public class RecipeController {
    /** 建立RecipeSer的物件recipeService,來調用 */
    private  final RecipeService recipeService;
    /**
     * RecipeController 的建構子。
     * 透過依賴注入 (Dependency Injection) 的方式接收 RecipeService 實例。
     * @param recipeService 處理食譜業務邏輯的服務物件。
     */
    public RecipeController(RecipeService recipeService){
        this.recipeService=recipeService;
    }
    /**
     * 根據客戶端提供的請求生成食譜。
     * 該方法會接收一個包含生成食譜所需資訊 (例如食材、口味偏好等) 的請求物件，
     * 並呼叫 RecipeService 來實際生成食譜。
     * @param request 包含生成食譜所需參數的請求物件。
     * @return 包含生成食譜結果的響應物件。
     */
    @PostMapping("/generate")
    public RecipeResponse generateRecipe(@RequestBody RecipeRequest request) {
        return recipeService.generateRecipe(request);
    }
}
