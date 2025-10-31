package com.example.recipe_ai.entity;
import jakarta.persistence.*;
import  java.time.Instant;
import java.util.List;

import  jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;     //存大量文字
import lombok.Getter;
import lombok.Setter;

@Entity // 告訴spring boot，這個class是一個(entity)資料庫中一個表格的映射
@Setter
@Getter
@Table(name = "recipe_cache") //告訴spring boot，這個class對應的資料表名稱
public class RecipeCache {
    //1-主鍵宣告
    @Id
    @Column(name="key_id",length = 512)
    private  String key_id;
    //2-食譜標題column
    @Column(name = "title",length = 512)
    private String title;
    //3-食譜食材column
    @Lob    //大量文字
    @Column(name = "ingredients",columnDefinition = "TEXT")
    private String ingredients;
    //4-料理步驟column
    @Column(name = "steps",columnDefinition = "MEDIUMTEXT")
    private String steps;
    //5-圖片url
    @Column(name = "imageUrl",columnDefinition = "MEDIUMTEXT")
    private  String imageUrl;

    //jpa需要無參數的建構子呼叫
    public  RecipeCache(){
    }
    //初始化的建構子
    public RecipeCache(String key_id,String title,String ingredients,String steps, String imageUrl){
        this.key_id=key_id;
        this.title=title;
        this.ingredients=ingredients;
        this.steps=steps;
        this.imageUrl=imageUrl;
    }

}
