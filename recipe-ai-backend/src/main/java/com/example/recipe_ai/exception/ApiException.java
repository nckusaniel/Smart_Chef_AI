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
        // 'super(message)'：呼叫父類別（RuntimeException）的建構子，並把 'message' 參數傳上去。
        // 這樣，這個 'ApiException' 物件就繼承了儲存錯誤訊息的功能。
        // 之後你可以使用 .getMessage() 方法來取得這個 "message"。
        super(message);
        //把從外面傳進來的 HTTP 狀態碼，儲存到這個物件內部的 'status' 變數中
        this.status = status;
    }

}
