package com.example.recipe_ai.exception;
import org.springframework.http.HttpStatus;
import lombok.Data;
@Data
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    /**
     * 建構子
     * @param message 錯誤訊息
     * @param status  對應的 HTTP 狀態
     */
    public ApiException(String message, HttpStatus status) {
        // 呼叫父類別 RuntimeException，再呼叫 父類別 Throwable 保存，就會存下這個message
        // 之後可以使用 getMessage() 取得錯誤訊息。
        super(message);
        //把從外面傳進來的 HTTP 狀態碼，儲存到這個物件內部的 'status' 變數中
        this.status = status;
    }
}

//Object
// └─ Throwable
//      ├─ Error
//      └─ Exception
//           └─ RuntimeException
//                └─ ApiException