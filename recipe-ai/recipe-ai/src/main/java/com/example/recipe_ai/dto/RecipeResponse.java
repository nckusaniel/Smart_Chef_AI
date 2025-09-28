package com.example.recipe_ai.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * RecipeResponse
 * 後端回傳給前端的 DTO
 * 包含食譜完整資訊：標題、食材清單、步驟、圖片
 */
@JsonDeserialize(builder = RecipeResponse.RecipeResponseBuilder.class)
@Data
@Builder
public class RecipeResponse {
    private String title;               // 食譜名稱
    private List<String> ingredients;   // 食材清單
    private List<String> steps;         // 料理步驟
    private String imageUrl;            // 成品圖片網址
    @JsonPOJOBuilder(withPrefix = "")
    public static class RecipeResponseBuilder {
    }
}
