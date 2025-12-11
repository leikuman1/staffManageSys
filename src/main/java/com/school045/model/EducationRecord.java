package com.school045.model;

import java.time.LocalDate;

public record EducationRecord(int id, int staffId, String degree, String major, LocalDate startDate, LocalDate endDate) {
}
