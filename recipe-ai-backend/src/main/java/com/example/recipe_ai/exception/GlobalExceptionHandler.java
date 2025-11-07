package com.example.recipe_ai.exception;
// ResponseEntity 代表一個完整的 HTTP 回應，它包含了 HTTP 狀態碼、標頭（Headers）以及回應主體（Body）。
// 我們用它來精確控制要回傳給前端的內容和狀態碼。
import org.springframework.http.ResponseEntity;
// import @ExceptionHandler 註解，告訴 Spring，下面的這個方法用來處理特定種類的異常（Exception）
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

// @RestControllerAdvice 告訴 Spring Boot：「請掃描這個類別，把它當作一個全域的異常處理器」。
// 任何Controller拋出異常時，Spring 都會來這個類別裡尋找對應方法
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 如果專案中出現ApiException，執行下面方法，e是 ApiException，e可以 .getMessage()、 getStatus()。
    @ExceptionHandler(ApiException.class)
    //handleApiException、把 ApiException 轉成前端能讀懂的 JSON 與 HTTP 格式
    public ResponseEntity <Map<String, Object>> handleApiException(ApiException e) {
        //---統一格式回傳前端、訊息、http狀態 打包成 json 格式map。
        Map<String,Object> exception_response =Map.of(
                "message", e.getMessage(),
                "status", e.getStatus().value()
        );
        return new ResponseEntity<>(exception_response, e.getStatus());
    }
}
//exception_response
// {
//  "message": "AI JSON 解析失敗",
//  "status": 500
//}