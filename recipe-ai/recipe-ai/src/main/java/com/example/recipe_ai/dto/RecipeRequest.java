package com.example.recipe_ai.dto;

import lombok.Data;

@Data
public  class RecipeRequest  {
    private String ingredients;     //輸入食材
    private String styleOrDiet;     //輸入飲食需求/料理風格
}

//接收 post json{
//  "ingredients": "雞肉, 洋蔥",
//  "styleOrDiet": "低碳"
//}
