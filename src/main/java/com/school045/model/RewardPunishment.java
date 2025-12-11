package com.school045.model;

import java.time.LocalDate;

public record RewardPunishment(int id, int staffId, String type, String description, LocalDate occurDate) {
}
