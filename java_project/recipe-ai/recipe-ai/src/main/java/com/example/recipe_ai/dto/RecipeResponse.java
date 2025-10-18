package com.example.recipe_ai.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import lombok.NoArgsConstructor;   // <-- 1. 匯入這兩個
import lombok.AllArgsConstructor; // <-- 1. 匯入這兩個

/**
 * RecipeResponse
 * 後端回傳給前端的 DTO
 * (不再需要複雜的 @Json... 註解了)
 */
@Data                // 1. 提供 get... 和 set... (Jackson 和你都需要)
@Builder             // 2. 提供 .builder() (你想要)
@NoArgsConstructor   // 3. 強制加上「無參數建構子」 new RecipeResponse() (Jackson 需要)
@AllArgsConstructor  // 4. 強制加上「全參數建構子」 (確保 @Builder 能正常運作)
public class RecipeResponse {
    private String title;
    private List<String> ingredients;
    private List<String> steps;
    private String imageUrl;
}