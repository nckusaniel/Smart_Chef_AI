package com.example.recipe_ai.controller;
// ====================== DTO ======================
import com.example.recipe_ai.dto.RecipeRequest;
import com.example.recipe_ai.dto.RecipeResponse;
// ====================== Service ======================
//處理核心業務邏輯（生成食譜、處理資料、呼叫 AI 等
import com.example.recipe_ai.service.RecipeService;
import org.springframework.web.bind.annotation.*;

// 標記這個類別 (Class) 是一個「API 控制器」，接收前端的網路請求並「回傳 JSON 資料」
// @controller負責接收 HTTP 請求、處理資料，並回傳資料或 HTML 頁面等。 @ResponseBody。回傳值放到 HTTP Response Body 中回傳給到前端
@RestController
// 允許「所有來源」的網址，都可以來呼叫這裡的 API。解決 CORS 跨域問題。
@CrossOrigin(origins = "*")
// 統一此class控制器裡所有 API 的「路徑」，都以 "/api/recipe" 開頭
@RequestMapping("/api/recipe")

/**
 * Recipe Controller負責處理前端 HTTP 請求，並呼叫後端的生成食譜並回傳給前端。
 */
public class RecipeController {

    // 宣告RecipeService型態的變數my_recipeService
    // 把RecipeService的注入myrecipeService來使用
    private final RecipeService myrecipeService;
    public RecipeController(RecipeService recipeService) {
        this.myrecipeService = recipeService;     //附值給自己
    }

    /**
     * 這是一個產生食譜 API，名為handle_generateRecipe
     * @param request - 顧客遞給你的「點單」 (RecipeRequest)
     * @return RecipeResponse - 你端回給顧客的「完成的菜餚」 (RecipeResponse)
     */
    //利用myrecipeService呼叫generateRecipe方法(參數為requset)，回傳resoponse到前端
    @PostMapping("/generate")
    public RecipeResponse handle_generateRecipe(@RequestBody RecipeRequest request) {
        return myrecipeService.generateRecipe(request);
    }
}
