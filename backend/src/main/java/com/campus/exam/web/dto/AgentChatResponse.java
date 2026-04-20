package com.campus.exam.web.dto;

public record AgentChatResponse(
        String reply,
        /** true 表示来自大模型 API；false 为内置规则/离线回复 */
        boolean fromLlm
) {
}
