package com.campus.exam.service;

import com.campus.exam.domain.MessageStatus;
import com.campus.exam.domain.StudentMessage;
import com.campus.exam.domain.UserAccount;
import com.campus.exam.repository.StudentMessageRepository;
import com.campus.exam.repository.UserAccountRepository;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.web.dto.StudentMessageCreateRequest;
import com.campus.exam.web.dto.StudentMessageDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class StudentMessageService {

    private final StudentMessageRepository studentMessageRepository;
    private final UserAccountRepository userAccountRepository;

    public StudentMessageService(
            StudentMessageRepository studentMessageRepository,
            UserAccountRepository userAccountRepository) {
        this.studentMessageRepository = studentMessageRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public StudentMessageDto create(AuthenticatedUser user, StudentMessageCreateRequest req) {
        StudentMessage m = new StudentMessage();
        m.setUserId(user.id());
        m.setSubject(req.subject().trim());
        m.setCourse(req.course().trim());
        m.setClassName(req.className().trim());
        m.setContent(req.content().trim());
        m.setStatus(MessageStatus.PENDING);
        studentMessageRepository.save(m);
        UserAccount stu = userAccountRepository.findById(user.id()).orElse(null);
        return toDto(m, stu, null);
    }

    public List<StudentMessageDto> listMine(AuthenticatedUser user) {
        return studentMessageRepository.findByUserIdOrderByCreatedAtDesc(user.id()).stream()
                .map(m -> {
                    UserAccount stu = userAccountRepository.findById(m.getUserId()).orElse(null);
                    UserAccount replier = m.getRepliedByUserId() != null
                            ? userAccountRepository.findById(m.getRepliedByUserId()).orElse(null)
                            : null;
                    return toDto(m, stu, replier);
                })
                .toList();
    }

    public List<StudentMessageDto> listAllForStaff() {
        return studentMessageRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(m -> {
                    UserAccount stu = userAccountRepository.findById(m.getUserId()).orElse(null);
                    UserAccount replier = m.getRepliedByUserId() != null
                            ? userAccountRepository.findById(m.getRepliedByUserId()).orElse(null)
                            : null;
                    return toDto(m, stu, replier);
                })
                .toList();
    }

    @Transactional
    public StudentMessageDto reply(AuthenticatedUser staff, Long messageId, String replyText) {
        StudentMessage m = studentMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        m.setReplyContent(replyText.trim());
        m.setRepliedByUserId(staff.id());
        m.setRepliedAt(Instant.now());
        m.setStatus(MessageStatus.REPLIED);
        studentMessageRepository.save(m);
        UserAccount stu = userAccountRepository.findById(m.getUserId()).orElse(null);
        UserAccount replier = userAccountRepository.findById(staff.id()).orElse(null);
        return toDto(m, stu, replier);
    }

    private static StudentMessageDto toDto(StudentMessage m, UserAccount student, UserAccount replier) {
        return new StudentMessageDto(
                m.getId(),
                m.getUserId(),
                student != null ? student.getDisplayName() : null,
                student != null ? student.getUsername() : null,
                m.getSubject(),
                m.getCourse(),
                m.getClassName(),
                m.getContent(),
                m.getStatus() != null ? m.getStatus().name() : MessageStatus.PENDING.name(),
                m.getReplyContent(),
                m.getRepliedByUserId(),
                replier != null ? replier.getDisplayName() : null,
                m.getRepliedAt(),
                m.getCreatedAt());
    }
}
