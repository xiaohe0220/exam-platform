package com.campus.exam.util;

import com.campus.exam.domain.Question;
import com.campus.exam.domain.QuestionType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * 选择题选项乱序：perm[displayIndex] = 显示在该位置的原始选项下标（0..n-1）。
 */
public final class OptionShuffleUtil {

    private static final ObjectMapper M = new ObjectMapper();

    private OptionShuffleUtil() {
    }

    /** perm[显示位] = 该位置展示的「原始选项」下标 */
    public static List<Integer> randomPermutation(int n) {
        List<Integer> perm = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            perm.add(i);
        }
        Collections.shuffle(perm);
        return perm;
    }

    public static List<String> applyPermToOptions(List<String> originals, List<Integer> perm) {
        List<String> out = new ArrayList<>(perm.size());
        for (int d = 0; d < perm.size(); d++) {
            out.add(originals.get(perm.get(d)));
        }
        return out;
    }

    public static String shuffleOptionsJson(String optionsJson, List<Integer> perm) {
        try {
            List<String> orig = M.readValue(optionsJson, new TypeReference<>() {
            });
            if (orig.size() != perm.size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "选项数量异常");
            }
            return M.writeValueAsString(applyPermToOptions(orig, perm));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "选项解析失败");
        }
    }

    /**
     * 将学生按「展示顺序」填写的字母转为原始题库中的选项字母。
     */
    public static Map<Long, String> toOriginalAnswers(
            Map<Long, String> displayAnswers,
            String shuffleJson,
            Map<Long, Question> qmap) {
        if (shuffleJson == null || shuffleJson.isBlank()) {
            return displayAnswers;
        }
        try {
            Map<String, List<Integer>> shuffle =
                    M.readValue(shuffleJson, new TypeReference<>() {
                    });
            Map<Long, String> out = new HashMap<>(displayAnswers);
            for (Map.Entry<Long, String> en : displayAnswers.entrySet()) {
                Long qid = en.getKey();
                Question q = qmap.get(qid);
                if (q == null) {
                    continue;
                }
                List<Integer> perm = shuffle.get(String.valueOf(qid));
                if (perm == null || perm.isEmpty()) {
                    continue;
                }
                if (q.getType() == QuestionType.SINGLE_CHOICE) {
                    String origLetter = displayToOriginalLetter(en.getValue(), perm);
                    out.put(qid, origLetter);
                } else if (q.getType() == QuestionType.MULTIPLE_CHOICE) {
                    out.put(qid, displayMultiToOriginalJson(en.getValue(), perm));
                }
            }
            return out;
        } catch (Exception e) {
            return displayAnswers;
        }
    }

    private static String displayToOriginalLetter(String displayRaw, List<Integer> perm) {
        if (displayRaw == null || displayRaw.isBlank()) {
            return displayRaw;
        }
        String d = displayRaw.trim().substring(0, 1).toUpperCase();
        int di = d.charAt(0) - 'A';
        if (di < 0 || di >= perm.size()) {
            return displayRaw;
        }
        int origIdx = perm.get(di);
        return String.valueOf((char) ('A' + origIdx));
    }

    private static String displayMultiToOriginalJson(String displayRaw, List<Integer> perm) throws Exception {
        Set<String> origSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        List<String> displayLetters;
        String t = displayRaw.trim();
        if (t.startsWith("[")) {
            displayLetters = M.readValue(t, new TypeReference<>() {
            });
        } else {
            displayLetters = List.of(t);
        }
        for (String dl : displayLetters) {
            origSet.add(displayToOriginalLetter(dl, perm));
        }
        return M.writeValueAsString(new ArrayList<>(origSet));
    }
}
