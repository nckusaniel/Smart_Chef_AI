package com.example.recipe_ai.repository;
import com.example.recipe_ai.entity.RecipeCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//@Repository可省略，但還是要標示讓人知道這class是實作Repository
@Repository
//JpaRepository<table,主鍵型態>
public interface RecipeCacheRepository extends
        JpaRepository<RecipeCache,String> {
}
// JpaRepository 自動產生以下方法
// - save(RecipeCache entity)     // 新增或更新
// - findById(String requestKey)  // 透過主鍵查詢
// - deleteById(String requestKey) // 刪除
