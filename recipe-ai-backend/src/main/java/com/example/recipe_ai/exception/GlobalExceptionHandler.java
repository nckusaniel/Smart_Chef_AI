package com.example.recipe_ai.exception;
// ResponseEntity 代表一個完整的 HTTP 回應，它包含了 HTTP 狀態碼、標頭（Headers）以及回應主體（Body）。
// 我們用它來精確控制要回傳給前端的內容和狀態碼。
import org.springframework.http.ResponseEntity;
// import @ExceptionHandler 註解，告訴 Spring，下面的這個方法用來處理特定種類的異常（Exception）
import org.springframework.web.bind.annotation.ExceptionHandler;
//@RestControllerAdvice 組合註解（@ControllerAdvice + @ResponseBody），
// @ControllerAdvice 此類是一個「通知」或「顧問」，它會「監聽」所有 @Controller。
// @ResponseBody 這個類別中的方法預設會回傳 JSON 格式的資料。
// 總之，@RestControllerAdvice 就是向 Spring 宣告：「我是一個全域的 API 異常處理中心」。
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

// @RestControllerAdvice 告訴 Spring Boot：「請掃描這個類別，把它當作一個全域的異常處理器」。
// 任何Controller拋出異常時，Spring 都會來這個類別裡尋找對應方法。
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 當ApiException的例外被發現時，執行下面方法
    @ExceptionHandler(ApiException.class)
    //當Spring捕捉到 ApiException，把異常放進 'e' 變數中，我們從'e'取得錯誤訊息（e.getMessage()）和狀態碼（e.getStatus()）。
    public ResponseEntity <Map<String, Object>> handleApiException(ApiException e) {
        Map<String,Object> exception_response =Map.of(
                "message", e.getMessage(),
                "status", e.getStatus().value()
        );
        return new ResponseEntity<>(exception_response, e.getStatus());
    }
}
