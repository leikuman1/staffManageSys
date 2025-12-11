package com.school045.model;

import java.time.LocalDate;

public record Staff(
        int id,
        String staffCode,
        String fullName,
        String gender,
        String email,
        String phone,
        Integer departmentId,
        Integer positionId,
        Integer titleId,
        String status,
        LocalDate hireDate) {
}
