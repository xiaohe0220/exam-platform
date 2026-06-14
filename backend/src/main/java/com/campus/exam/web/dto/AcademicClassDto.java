package com.campus.exam.web.dto;

import com.campus.exam.domain.AcademicClass;

public record AcademicClassDto(
        Long id,
        String name,
        String college,
        String major,
        String grade,
        boolean enabled,
        long studentCount
) {
    public static AcademicClassDto of(AcademicClass c, long studentCount) {
        return new AcademicClassDto(
                c.getId(),
                c.getName(),
                c.getCollege(),
                c.getMajor(),
                c.getGrade(),
                !Boolean.FALSE.equals(c.getEnabled()),
                studentCount);
    }
}
