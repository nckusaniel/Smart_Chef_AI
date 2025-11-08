package com.example.recipe_ai.exception;
// ResponseEntity 代表一個完整的 HTTP 回應，包含HTTP狀態、標頭、Body。
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
// @RestControllerAdvice是全域的異常處理器。有異常時，Spring來這個類別找方法
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