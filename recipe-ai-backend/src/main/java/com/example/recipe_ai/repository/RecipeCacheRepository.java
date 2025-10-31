package com.example.recipe_ai.repository;
import com.example.recipe_ai.entity.RecipeCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//告訴spring 這個class是一個資料庫存取元件
@Repository
//RecipeCacheRepository，繼承JPArepositary
// 1. 要管理的 Entity 是 RecipeCache
// 2. RecipeCache 的主鍵 (@Id) 類型是 String

public interface RecipeCacheRepository extends
        JpaRepository<RecipeCache,String> {
    // JpaRepository 自動產生了
    // - save(RecipeCache entity)     // 新增或更新
    // - findById(String requestKey)  // 透過主鍵查詢
    // - deleteById(String requestKey) // 刪除
}
