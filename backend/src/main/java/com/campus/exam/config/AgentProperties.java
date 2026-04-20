package com.campus.exam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.agent")
public class AgentProperties {

    /** 是否启用对外部大模型 API 的调用（未配置 api-key 时仍可使用内置规则回复） */
    private boolean enabled = true;

    /** OpenAI 兼容接口完整 URL，如 https://api.openai.com/v1/chat/completions */
    private String baseUrl = "https://api.openai.com/v1/chat/completions";

    private String apiKey = "";

    private String model = "gpt-4o-mini";

    private String systemPrompt =
            "你是「智能考试评测运维平台」的助手，面向师生。请用简洁中文回答，涉及考试规则、操作步骤时务必准确；若不确定请建议联系教务。";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
}
