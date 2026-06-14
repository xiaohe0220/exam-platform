package com.campus.exam.service;

import com.campus.exam.domain.AcademicClass;
import com.campus.exam.domain.Course;
import com.campus.exam.repository.AcademicClassRepository;
import com.campus.exam.repository.CourseRepository;
import com.campus.exam.repository.UserAccountRepository;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.web.dto.AcademicClassDto;
import com.campus.exam.web.dto.AcademicClassRequest;
import com.campus.exam.web.dto.CourseDto;
import com.campus.exam.web.dto.CourseRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AcademicCatalogService {

    private final AcademicClassRepository classRepository;
    private final CourseRepository courseRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuditService auditService;

    public AcademicCatalogService(
            AcademicClassRepository classRepository,
            CourseRepository courseRepository,
            UserAccountRepository userAccountRepository,
            AuditService auditService) {
        this.classRepository = classRepository;
        this.courseRepository = courseRepository;
        this.userAccountRepository = userAccountRepository;
        this.auditService = auditService;
    }

    public List<AcademicClassDto> classes() {
        return classRepository.findAll().stream()
                .map(c -> AcademicClassDto.of(c, userAccountRepository.findByClassName(c.getName()).size()))
                .toList();
    }

    @Transactional
    public AcademicClassDto createClass(AuthenticatedUser user, AcademicClassRequest req) {
        classRepository.findByName(req.name().trim()).ifPresent(x -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "班级名称已存在");
        });
        AcademicClass c = new AcademicClass();
        apply(c, req);
        classRepository.save(c);
        auditService.log(user.id(), "ACADEMIC_CLASS_CREATE", c.getName());
        return AcademicClassDto.of(c, userAccountRepository.findByClassName(c.getName()).size());
    }

    @Transactional
    public AcademicClassDto updateClass(AuthenticatedUser user, Long id, AcademicClassRequest req) {
        AcademicClass c = classRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        classRepository.findByName(req.name().trim()).ifPresent(x -> {
            if (!x.getId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "班级名称已存在");
            }
        });
        apply(c, req);
        auditService.log(user.id(), "ACADEMIC_CLASS_UPDATE", "id=" + id);
        return AcademicClassDto.of(c, userAccountRepository.findByClassName(c.getName()).size());
    }

    @Transactional
    public void deleteClass(AuthenticatedUser user, Long id) {
        AcademicClass c = classRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        long students = userAccountRepository.findByClassName(c.getName()).size();
        if (students > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该班级仍有关联学生，不能删除");
        }
        classRepository.delete(c);
        auditService.log(user.id(), "ACADEMIC_CLASS_DELETE", "id=" + id);
    }

    public List<CourseDto> courses() {
        return courseRepository.findAll().stream().map(CourseDto::of).toList();
    }

    @Transactional
    public CourseDto createCourse(AuthenticatedUser user, CourseRequest req) {
        String code = clean(req.code());
        if (code != null) {
            courseRepository.findByCode(code).ifPresent(x -> {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "课程代码已存在");
            });
        }
        Course c = new Course();
        apply(c, req);
        courseRepository.save(c);
        auditService.log(user.id(), "COURSE_CREATE", c.getName());
        return CourseDto.of(c);
    }

    @Transactional
    public CourseDto updateCourse(AuthenticatedUser user, Long id, CourseRequest req) {
        Course c = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String code = clean(req.code());
        if (code != null) {
            courseRepository.findByCode(code).ifPresent(x -> {
                if (!x.getId().equals(id)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "课程代码已存在");
                }
            });
        }
        apply(c, req);
        auditService.log(user.id(), "COURSE_UPDATE", "id=" + id);
        return CourseDto.of(c);
    }

    @Transactional
    public void deleteCourse(AuthenticatedUser user, Long id) {
        Course c = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        courseRepository.delete(c);
        auditService.log(user.id(), "COURSE_DELETE", "id=" + id);
    }

    private static void apply(AcademicClass c, AcademicClassRequest req) {
        c.setName(req.name().trim());
        c.setCollege(clean(req.college()));
        c.setMajor(clean(req.major()));
        c.setGrade(clean(req.grade()));
        c.setEnabled(req.enabled() == null || req.enabled());
    }

    private static void apply(Course c, CourseRequest req) {
        c.setCode(clean(req.code()));
        c.setName(req.name().trim());
        c.setCollege(clean(req.college()));
        c.setTeacherName(clean(req.teacherName()));
        c.setEnabled(req.enabled() == null || req.enabled());
    }

    private static String clean(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
