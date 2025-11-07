package com.example.recipe_ai.entity;
import jakarta.persistence.*;
import  jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;     //存大量文字
import lombok.Getter;
import lombok.Setter;

@Entity // 告訴spring boot，這個class是一個(entity)，資料庫中的table
@Setter
@Getter
@Table(name = "recipe_cache") //告訴spring boot，這個class對應的資料表名稱
public class RecipeCache {
    //1-欄位  主鍵
    @Id
    @Column(name="key_id",length = 512)
    private  String key_id;

    //2-欄位  料理名稱
    @Column(name = "title",length = 512)
    private String title;

    //3-欄位  食材清單
    @Lob    //大量文字
    @Column(name = "ingredients",columnDefinition = "TEXT")
    private String ingredients;

    //4-欄位  料理步驟
    @Column(name = "steps",columnDefinition = "MEDIUMTEXT")
    private String steps;

    //5-欄位  料理預覽圖
    @Column(name = "imageUrl",columnDefinition = "MEDIUMTEXT")
    private  String imageUrl;

    //jpa需要無參數的建構子呼叫
    public  RecipeCache(){
    }

}
