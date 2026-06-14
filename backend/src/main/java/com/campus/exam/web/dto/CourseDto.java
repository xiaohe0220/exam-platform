package com.campus.exam.web.dto;

import com.campus.exam.domain.Course;

public record CourseDto(
        Long id,
        String code,
        String name,
        String college,
        String teacherName,
        boolean enabled
) {
    public static CourseDto of(Course c) {
        return new CourseDto(
                c.getId(),
                c.getCode(),
                c.getName(),
                c.getCollege(),
                c.getTeacherName(),
                !Boolean.FALSE.equals(c.getEnabled()));
    }
}
