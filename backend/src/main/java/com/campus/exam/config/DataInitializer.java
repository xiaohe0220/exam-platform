package com.campus.exam.config;

import com.campus.exam.domain.*;
import com.campus.exam.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner seed(
            UserAccountRepository users,
            QuestionRepository questions,
            ExamRepository exams,
            ExamQuestionRepository examQuestions,
            PasswordEncoder encoder) {
        return args -> {
            if (users.count() > 0) {
                return;
            }
            log.info("Seeding demo data...");

            UserAccount admin = user("admin", "admin123", "教务处管理员", UserRole.ADMIN, null, encoder);
            UserAccount teacher = user("teacher", "teacher123", "张老师", UserRole.TEACHER, null, encoder);
            UserAccount student = user("student", "student123", "李小明", UserRole.STUDENT, "计算机2101", encoder);
            users.save(admin);
            users.save(teacher);
            users.save(student);

            Long tid = teacher.getId();

            Question q1 = q(tid, QuestionType.SINGLE_CHOICE, "人工智能的含义是？",
                    "[\"通过计算实现类人智能行为\",\"仅指深度学习\",\"仅指专家系统\",\"与数据无关\"]",
                    "\"A\"", 2, "第一章", "AI概念");

            Question q2 = q(tid, QuestionType.MULTIPLE_CHOICE, "下列属于监督学习的有？",
                    "[\"线性回归\",\"K-means聚类\",\"决策树（有标签）\",\"随机猜测\"]",
                    "[\"A\",\"C\"]", 3, "第二章", "机器学习");

            Question q3 = q(tid, QuestionType.TRUE_FALSE, "神经网络层数越深一定越好。",
                    "[\"正确\",\"错误\"]",
                    "false", 2, "第三章", "深度学习");

            Question q4 = q(tid, QuestionType.FILL_BLANK, "常用优化算法之一：_____（小写英文 Adam）",
                    null,
                    "\"adam\"", 2, "第四章", "优化");

            Question q5 = q(tid, QuestionType.SHORT_ANSWER, "简述你对“可解释人工智能”的理解（不少于20字）。",
                    null,
                    null, 3, "第五章", "伦理");

            questions.save(q1);
            questions.save(q2);
            questions.save(q3);
            questions.save(q4);
            questions.save(q5);

            Exam ex = new Exam();
            ex.setCreatorId(tid);
            ex.setTitle("人工智能通识 - 样例期末考试");
            ex.setDescription("覆盖课程主干知识点，客观题自动批阅，简答题由教师批阅。");
            Instant now = Instant.now();
            ex.setStartAt(now.minus(1, ChronoUnit.HOURS));
            ex.setEndAt(now.plus(30, ChronoUnit.DAYS));
            ex.setDurationMinutes(60);
            ex.setMaxRetakes(2);
            ex.setSwitchLimit(3);
            ex.setFullscreenRequired(true);
            ex.setTargetClasses("计算机2101");
            ex.setStatus(ExamStatus.PUBLISHED);
            exams.save(ex);

            int order = 1;
            link(examQuestions, ex.getId(), q1.getId(), order++, BigDecimal.valueOf(20));
            link(examQuestions, ex.getId(), q2.getId(), order++, BigDecimal.valueOf(20));
            link(examQuestions, ex.getId(), q3.getId(), order++, BigDecimal.valueOf(20));
            link(examQuestions, ex.getId(), q4.getId(), order++, BigDecimal.valueOf(20));
            link(examQuestions, ex.getId(), q5.getId(), order++, BigDecimal.valueOf(20));

            log.info("Demo exam id={} published for class 计算机2101", ex.getId());
        };
    }

    private static void link(ExamQuestionRepository repo, Long examId, Long qid, int ord, BigDecimal score) {
        ExamQuestion eq = new ExamQuestion();
        eq.setExamId(examId);
        eq.setQuestionId(qid);
        eq.setOrderIndex(ord);
        eq.setScore(score);
        repo.save(eq);
    }

    private static Question q(
            Long creator,
            QuestionType type,
            String title,
            String options,
            String ans,
            int diff,
            String chapter,
            String kp) {
        Question q = new Question();
        q.setCreatorId(creator);
        q.setType(type);
        q.setTitle(title);
        q.setContent("<p>" + title + "</p>");
        q.setOptionsJson(options);
        if (ans != null) {
            q.setCorrectAnswerJson(ans);
        }
        q.setDifficulty(diff);
        q.setChapter(chapter);
        q.setKnowledgePoint(kp);
        return q;
    }

    private static UserAccount user(
            String u,
            String pass,
            String name,
            UserRole role,
            String className,
            PasswordEncoder enc) {
        UserAccount a = new UserAccount();
        a.setUsername(u);
        a.setPasswordHash(enc.encode(pass));
        a.setDisplayName(name);
        a.setRole(role);
        a.setClassName(className);
        a.setCollege("计算机学院");
        return a;
    }
}
