package com.example.recipe_ai.config; // 你可以把這個 .java 檔放在 `config` 資料夾

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Spring 應用程式配置
 * 這裡定義了應用程式需要注入 (Inject) 到其他地方的共用 Bean (物件)
 */
@Configuration
public class AppConfig {

    /**
     * 定義一個 RestTemplate Bean
     * 當 Spring 看到有其他地方 (像我們的 Service) 需要 @Autowired RestTemplate 時,
     * 它就會呼叫這個方法，並將回傳的 new RestTemplate() 物件交出去。
     * @return 一個新的 RestTemplate 實例
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 定義一個 ObjectMapper Bean
     * 這對 JSON 序列化/反序列化是必要的
     * @return 一個新的 ObjectMapper 實例
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
