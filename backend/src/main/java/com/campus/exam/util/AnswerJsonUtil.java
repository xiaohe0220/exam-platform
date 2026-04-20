package com.campus.exam.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class AnswerJsonUtil {

    private static final ObjectMapper M = new ObjectMapper();

    private AnswerJsonUtil() {
    }

    public static Map<Long, String> toMap(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            Map<String, Object> raw = M.readValue(json, new TypeReference<>() {
            });
            Map<Long, String> out = new HashMap<>();
            for (Map.Entry<String, Object> en : raw.entrySet()) {
                Long qid = Long.parseLong(en.getKey());
                Object v = en.getValue();
                if (v == null) {
                    out.put(qid, "");
                } else if (v instanceof Collection<?> || v instanceof Map<?, ?>) {
                    out.put(qid, M.writeValueAsString(v));
                } else {
                    out.put(qid, String.valueOf(v));
                }
            }
            return out;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
