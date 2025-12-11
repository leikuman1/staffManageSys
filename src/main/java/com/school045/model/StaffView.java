package com.school045.model;

public record StaffView(
        int id,
        String staffCode,
        String fullName,
        String department,
        String position,
        String title,
        String status,
        String email) {
}
