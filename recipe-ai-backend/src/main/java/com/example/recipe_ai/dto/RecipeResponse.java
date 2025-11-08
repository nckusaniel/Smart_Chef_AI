package com.example.recipe_ai.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import lombok.NoArgsConstructor;   // <-- 1. 匯入這兩個
import lombok.AllArgsConstructor; // <-- 1. 匯入這兩個

/**
 * RecipeResponse
 * 後端回傳給前端的 DTO
 */
@Data                // 1. 提供 get... 和 set... (Jackson 和你都需要)
@Builder             // 2. 提供 .builder() (你想要)
@NoArgsConstructor   // 3. 強制加上「無參數建構子」 new RecipeResponse() (Jackson 需要)
@AllArgsConstructor  // 4. 強制加上「全參數建構子」 (確保 @Builder 能正常運作)
public class RecipeResponse {
    private String title;               //料理名稱
    private List<String> ingredients;   //食材清單
    private List<String> steps;         //料理步驟
    private String imageUrl;            //料理預覽圖
}