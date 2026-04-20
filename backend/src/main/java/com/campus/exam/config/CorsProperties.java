package com.campus.exam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    /**
     * 允许的浏览器 Origin，生产环境请通过配置或环境变量覆盖。
     */
    private List<String> allowedOrigins = new ArrayList<>(List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173"));
    /**
     * 是否允许任意 Origin 访问（公网开放时可开启）。
     * 开启后将使用 allowedOriginPatterns=["*"]，并在 CORS 中关闭 credentials。
     */
    private boolean allowAllOrigins = false;

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public boolean isAllowAllOrigins() {
        return allowAllOrigins;
    }

    public void setAllowAllOrigins(boolean allowAllOrigins) {
        this.allowAllOrigins = allowAllOrigins;
    }
}
