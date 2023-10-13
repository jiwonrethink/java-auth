package config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // CORS를 적용할 URL패턴 정의
                .allowedOrigins("*") // 자원 공유를 허락할 Origin 지정
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS") // 허용할 HTTP method 지정
                .allowCredentials(false)
                .maxAge(3000); // 설정 시간만큼 pre-flight 리퀘스트 캐싱
    }
}
