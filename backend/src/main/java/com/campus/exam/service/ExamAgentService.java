package com.campus.exam.service;

import com.campus.exam.config.AgentProperties;
import com.campus.exam.web.dto.AgentChatResponse;
import com.campus.exam.web.dto.AgentStatusDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Locale;

/**
 * 考试平台智能体：可对接 OpenAI 兼容 Chat Completions；无密钥时使用内置规则回复。
 */
@Service
public class ExamAgentService {

    private static final Logger log = LoggerFactory.getLogger(ExamAgentService.class);

    private final AgentProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient;

    public ExamAgentService(AgentProperties props) {
        this.props = props;
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(15_000);
        rf.setReadTimeout(120_000);
        this.restClient = RestClient.builder().requestFactory(rf).build();
    }

    public AgentChatResponse respond(String userMessage) {
        if (!props.isEnabled()) {
            return new AgentChatResponse("智能助手当前已关闭，请联系管理员在配置中开启 app.agent.enabled。", false);
        }
        String key = props.getApiKey();
        if (key == null || key.isBlank()) {
            return new AgentChatResponse(fallbackReply(userMessage), false);
        }
        try {
            String body = buildRequestBody(userMessage);
            String raw = restClient.post()
                    .uri(props.getBaseUrl())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + key.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            String content = extractContent(raw);
            if (content == null || content.isBlank()) {
                return new AgentChatResponse(fallbackReply(userMessage), false);
            }
            return new AgentChatResponse(content.trim(), true);
        } catch (RestClientException e) {
            log.warn("智能体调用外部 API 失败: {}", e.getMessage());
            return new AgentChatResponse(
                    "暂时无法连接大模型服务，已切换为本地提示：\n" + fallbackReply(userMessage), false);
        } catch (Exception e) {
            log.warn("智能体解析响应失败", e);
            return new AgentChatResponse(fallbackReply(userMessage), false);
        }
    }

    public AgentStatusDto status() {
        String key = props.getApiKey();
        return new AgentStatusDto(
                props.isEnabled(),
                key != null && !key.isBlank(),
                props.getModel());
    }

    private String buildRequestBody(String userMessage) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", props.getModel());
        ArrayNode messages = root.putArray("messages");
        ObjectNode sys = messages.addObject();
        sys.put("role", "system");
        sys.put("content", props.getSystemPrompt());
        ObjectNode user = messages.addObject();
        user.put("role", "user");
        user.put("content", userMessage);
        return objectMapper.writeValueAsString(root);
    }

    private String extractContent(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isMissingNode()) {
            return null;
        }
        return content.asText();
    }

    private String fallbackReply(String msg) {
        if (msg == null || msg.isBlank()) {
            return "请描述你的问题，例如：如何参加考试、切屏规则、查看成绩等。";
        }
        String m = msg.toLowerCase(Locale.ROOT);
        String t = msg;

        if (containsAny(t, "切屏", "切换", "窗口")) {
            return "切屏次数由每场考试单独设定。超过允许的切屏次数可能会自动交卷，请以页面提示为准。考试中途请尽量避免切换应用或最小化窗口。";
        }
        if (containsAny(t, "全屏")) {
            return "若教师开启「全屏考试」，进入答题后请保持浏览器全屏。退出全屏可能记为异常行为，请按考场要求操作。";
        }
        if (containsAny(t, "交卷", "提交")) {
            return "在答题页完成作答后，使用「交卷」按钮提交。提交前请确认已保存答案。提交后一般不可再修改，具体以教师设置为准。";
        }
        if (containsAny(t, "考试", "答题", "参加")) {
            return "学生登录后，在「考试大厅」查看可参加的考试，点击「进入考试」开始答题。请留意开放时间与是否限制班级。";
        }
        if (containsAny(t, "成绩", "分数", "排名")) {
            return "交卷后可在「成绩分析」查看得分与排名等信息（具体展示以教师发布为准）。客观题通常自动批阅，主观题需教师阅卷。";
        }
        if (containsAny(t, "注册", "学号", "工号")) {
            return "可在登录页自主注册学生或教师账号。请使用本人真实学号或工号，系统会按学号/工号唯一校验，确保一人一账号；若提示已存在，说明该学号/工号已经注册。";
        }
        if (containsAny(t, "姓名", "汉字", "真实姓名")) {
            return "自主注册时姓名为必填项，只允许填写中文姓名；学号或工号作为唯一登录账号，请使用本人编号。若填写错误，可联系教务在用户管理中处理。";
        }
        if (containsAny(t, "密码", "忘记", "重置")) {
            return "已登录用户可在「个人资料」中修改密码。忘记密码时请联系教务管理员重置；演示环境可能会开放登录页重置入口。";
        }
        if (containsAny(t, "删除账号", "删号", "删除用户")) {
            return "教务可在「用户管理」中删除非当前登录账号。系统会阻止删除自己和最后一个管理员；已有业务记录的账号建议优先禁用，以保留考试与审计历史。";
        }
        if (containsAny(t, "题库筛选", "筛选题", "知识点", "难度")) {
            return "题库管理支持按关键词、文件夹、知识点、题型、难度筛选，并可切换分组/列表视图。导入大批题目后，建议先按文件夹或知识点缩小范围再编辑。";
        }
        if (containsAny(t, "导入", "批量", "模板")) {
            return "教师可在「题库管理」下载导入模板，按模板填写题型、题干、选项、答案、难度、文件夹和知识点后上传。导入后可通过题库筛选确认数量与分类。";
        }
        if (containsAny(t, "个性化", "主题", "偏好", "设置")) {
            return "可在「个性化设置」中调整主题色、紧凑布局、题库默认视图和智能助手默认展开。设置会保存到当前账号，重新登录后仍会生效。";
        }
        if (containsAny(t, "教师", "组卷", "题库", "阅卷")) {
            return "教师可在「题库管理」维护题目，在「考试发布」中组卷、发布与查看统计，并对主观题进行批阅。题库支持按文件夹、知识点、题型和难度快速定位。";
        }
        if (containsAny(t, "数据库", "mysql", "mariadb", "部署", "服务器")) {
            return "生产部署建议使用 MySQL/MariaDB 持久化数据，并把数据库密码、JWT 密钥和大模型 API Key 放在服务器环境变量或 systemd service 中，不要提交到 Git。部署后确认后端服务 active、Nginx 静态目录已更新。";
        }
        if (containsAny(t, "大模型", "api key", "agent", "助手配置")) {
            return "智能助手默认有离线规则。要接入大模型，请在服务器服务配置中设置 APP_AGENT_API_KEY，并按需设置 APP_AGENT_BASE_URL、APP_AGENT_MODEL，然后重启 exam-platform 服务。";
        }
        if (containsAny(m, "help", "hello", "你好", "您好")) {
            return "您好，我是考试平台智能助手。您可以问我：考试与交卷流程、切屏/全屏规则、成绩查询、注册与密码、教师组卷等。配置大模型 API 后回答会更灵活。";
        }

        return "我当前处于离线规则模式。您可以尝试问：考试流程、切屏、全屏、成绩、注册、教师组卷等。"
                + "\n\n部署时设置环境变量 APP_AGENT_API_KEY 并可选 APP_AGENT_BASE_URL / APP_AGENT_MODEL，即可接入兼容 OpenAI 的大模型接口。";
    }

    private static boolean containsAny(String text, String... keys) {
        for (String k : keys) {
            if (text.contains(k)) {
                return true;
            }
        }
        return false;
    }
}
