package com.example.recipe_ai.dto;

import lombok.Builder;
import lombok.Data;
/**
 * RecipeRequest
 * 使用者輸入資料的 DTO (Data Transfer Object)
 * 前端會以 JSON 格式 POST 過來
 */
@Builder
@Data
public class RecipeRequest {
    private String ingredients;   // 使用者輸入的食材 (例如: "雞肉, 洋蔥")
    private String styleOrDiet;   // 飲食需求 / 料理風格 (例如: "低碳", "泰式")
}

