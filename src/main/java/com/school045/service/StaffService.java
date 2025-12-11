package com.school045.service;

import com.school045.db.Database;
import com.school045.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StaffService {
    private final Database database;

    public StaffService(Database database) {
        this.database = database;
    }

    public List<Department> listDepartments() throws SQLException {
        String sql = "SELECT id, name, headcount FROM department045 ORDER BY name";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Department> items = new ArrayList<>();
            while (rs.next()) {
                items.add(new Department(rs.getInt("id"), rs.getString("name"), rs.getInt("headcount")));
            }
            return items;
        }
    }

    public List<Position> listPositions() throws SQLException {
        String sql = "SELECT id, name FROM position045 ORDER BY name";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Position> items = new ArrayList<>();
            while (rs.next()) {
                items.add(new Position(rs.getInt("id"), rs.getString("name")));
            }
            return items;
        }
    }

    public List<TitleInfo> listTitles() throws SQLException {
        String sql = "SELECT id, name FROM title045 ORDER BY name";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<TitleInfo> items = new ArrayList<>();
            while (rs.next()) {
                items.add(new TitleInfo(rs.getInt("id"), rs.getString("name")));
            }
            return items;
        }
    }

    public void saveDepartment(String name) throws SQLException {
        String sql = "INSERT INTO department045(name) VALUES (?) ON DUPLICATE KEY UPDATE name=VALUES(name)";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }

    public void savePosition(String name) throws SQLException {
        String sql = "INSERT INTO position045(name) VALUES (?) ON DUPLICATE KEY UPDATE name=VALUES(name)";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }

    public void saveTitle(String name) throws SQLException {
        String sql = "INSERT INTO title045(name) VALUES (?) ON DUPLICATE KEY UPDATE name=VALUES(name)";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }

    public void saveStaff(Staff staff) throws SQLException {
        String sql = """
                INSERT INTO staff045
                (staff_code, full_name, gender, email, phone, department_id, position_id, title_id, status, hire_date)
                VALUES (?,?,?,?,?,?,?,?,?,?)
                ON DUPLICATE KEY UPDATE
                    full_name=VALUES(full_name),
                    gender=VALUES(gender),
                    email=VALUES(email),
                    phone=VALUES(phone),
                    department_id=VALUES(department_id),
                    position_id=VALUES(position_id),
                    title_id=VALUES(title_id),
                    status=VALUES(status),
                    hire_date=VALUES(hire_date)
                """;
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staff.staffCode());
            ps.setString(2, staff.fullName());
            ps.setString(3, staff.gender());
            ps.setString(4, staff.email());
            ps.setString(5, staff.phone());
            setNullableInteger(ps, 6, staff.departmentId());
            setNullableInteger(ps, 7, staff.positionId());
            setNullableInteger(ps, 8, staff.titleId());
            ps.setString(9, staff.status());
            if (staff.hireDate() != null) {
                ps.setDate(10, Date.valueOf(staff.hireDate()));
            } else {
                ps.setNull(10, Types.DATE);
            }
            ps.executeUpdate();
        }
    }

    public Optional<Integer> findStaffIdByCode(String staffCode) throws SQLException {
        String sql = "SELECT id FROM staff045 WHERE staff_code=?";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staffCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt("id"));
                }
                return Optional.empty();
            }
        }
    }

    public void addEducation(EducationRecord record) throws SQLException {
        String sql = """
                INSERT INTO education045(staff_id, degree, major, start_date, end_date)
                VALUES (?,?,?,?,?)
                """;
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, record.staffId());
            ps.setString(2, record.degree());
            ps.setString(3, record.major());
            setDateOrNull(ps, 4, record.startDate());
            setDateOrNull(ps, 5, record.endDate());
            ps.executeUpdate();
        }
    }

    public void addFamilyRelation(FamilyRelation relation) throws SQLException {
        String sql = """
                INSERT INTO family045(staff_id, relation, name, contact, can_access)
                VALUES (?,?,?,?,?)
                """;
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, relation.staffId());
            ps.setString(2, relation.relation());
            ps.setString(3, relation.name());
            ps.setString(4, relation.contact());
            ps.setBoolean(5, relation.canAccess());
            ps.executeUpdate();
        }
    }

    public void addRewardOrPunishment(RewardPunishment record) throws SQLException {
        String sql = """
                INSERT INTO reward_punishment045(staff_id, type, description, occur_date)
                VALUES (?,?,?,?)
                """;
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, record.staffId());
            ps.setString(2, record.type());
            ps.setString(3, record.description());
            setDateOrNull(ps, 4, record.occurDate());
            ps.executeUpdate();
        }
    }

    public List<StaffView> searchStaff(String departmentFilter,
                                       String titleFilter,
                                       String positionFilter,
                                       String statusFilter) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT s.id,
                       s.staff_code,
                       s.full_name,
                       d.name AS department,
                       p.name AS position_name,
                       t.name AS title_name,
                       s.status,
                       s.email
                FROM staff045 s
                LEFT JOIN department045 d ON s.department_id = d.id
                LEFT JOIN position045 p ON s.position_id = p.id
                LEFT JOIN title045 t ON s.title_id = t.id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (departmentFilter != null && !departmentFilter.isBlank()) {
            sql.append(" AND d.name LIKE ?");
            params.add("%" + departmentFilter + "%");
        }
        if (titleFilter != null && !titleFilter.isBlank()) {
            sql.append(" AND t.name LIKE ?");
            params.add("%" + titleFilter + "%");
        }
        if (positionFilter != null && !positionFilter.isBlank()) {
            sql.append(" AND p.name LIKE ?");
            params.add("%" + positionFilter + "%");
        }
        if (statusFilter != null && !statusFilter.isBlank()) {
            sql.append(" AND s.status=?");
            params.add(statusFilter);
        }
        sql.append(" ORDER BY s.full_name");

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<StaffView> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(new StaffView(
                            rs.getInt("id"),
                            rs.getString("staff_code"),
                            rs.getString("full_name"),
                            rs.getString("department"),
                            rs.getString("position_name"),
                            rs.getString("title_name"),
                            rs.getString("status"),
                            rs.getString("email")
                    ));
                }
                return items;
            }
        }
    }

    public List<ReportRow> departmentTitleReport() throws SQLException {
        try (Connection conn = database.getConnection();
             Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("CALL sp_department_title_counts045()")) {
                return collectReportRows(rs);
            } catch (SQLException callEx) {
                // fall back to inline query when stored procedure is not present
                try (ResultSet rs = stmt.executeQuery("""
                        SELECT d.name AS department, t.name AS title, COUNT(s.id) AS staff_count
                        FROM department045 d
                        LEFT JOIN staff045 s ON s.department_id = d.id
                        LEFT JOIN title045 t ON s.title_id = t.id
                        GROUP BY d.name, t.name
                        ORDER BY d.name, t.name
                        """)) {
                    return collectReportRows(rs);
                }
            }
        }
    }

    private List<ReportRow> collectReportRows(ResultSet rs) throws SQLException {
        List<ReportRow> rows = new ArrayList<>();
        while (rs.next()) {
            rows.add(new ReportRow(
                    rs.getString("department"),
                    rs.getString("title"),
                    rs.getLong("staff_count")
            ));
        }
        return rows;
    }

    private void setNullableInteger(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private void setDateOrNull(PreparedStatement ps, int index, LocalDate value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(value));
        }
    }

    public LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value.trim());
    }
}
