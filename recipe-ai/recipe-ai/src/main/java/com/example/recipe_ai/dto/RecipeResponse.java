package com.example.recipe_ai.dto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RecipeResponse {
    private String title;  // 輸出食譜名稱
    private List<String> ingredients;  //輸出食材清單
    private List<String> steps;          //輸出食譜步驟
    private String imageUrl;           //輸出圖片網址
}
//{
//        "title": "蒜香雞胸肉",
//        "ingredients": ["雞胸肉", "蒜頭", "橄欖油"],
//        "steps": ["切蒜", "熱油", "下雞肉煎"],
//        "imageUrl": "https://example.com/image.jpg"
//        }
