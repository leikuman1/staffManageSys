package com.school045.ui;

import com.school045.model.*;
import com.school045.service.StaffService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class StaffMainPanel extends JPanel {
    private final StaffService service;

    private JComboBox<Department> departmentCombo;
    private JComboBox<Position> positionCombo;
    private JComboBox<TitleInfo> titleCombo;
    private JComboBox<String> statusCombo;

    private JTable departmentTable;
    private JTable positionTable;
    private JTable titleTable;
    private JTable staffTable;
    private JTable reportTable;

    private JTextField filterDepartmentField;
    private JTextField filterTitleField;
    private JTextField filterPositionField;
    private JComboBox<String> filterStatusCombo;

    public StaffMainPanel(StaffService service) {
        this.service = Objects.requireNonNull(service, "service");
        setLayout(new BorderLayout());
        add(buildTabs(), BorderLayout.CENTER);
        refreshDictionaries();
        refreshStaffTable();
        refreshReportTable();
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("组织主数据", createDictionaryTab());
        tabs.addTab("教职工管理", createStaffTab());
        tabs.addTab("学籍/家庭/奖惩", createRecordTab());
        tabs.addTab("查询报表", createReportTab());
        return tabs;
    }

    private JPanel createDictionaryTab() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 8, 0));
        panel.add(createDictionaryColumn("部门维护", "部门名称", name -> service.saveDepartment(name), deptTable()));
        panel.add(createDictionaryColumn("岗位/职务维护", "岗位名称", name -> service.savePosition(name), positionTable()));
        panel.add(createDictionaryColumn("职称维护", "职称名称", name -> service.saveTitle(name), titleTable()));
        return panel;
    }

    private JTable deptTable() {
        departmentTable = new JTable(new DefaultTableModel(new Object[]{"ID", "名称", "人数"}, 0));
        return departmentTable;
    }

    private JTable positionTable() {
        positionTable = new JTable(new DefaultTableModel(new Object[]{"ID", "名称"}, 0));
        return positionTable;
    }

    private JTable titleTable() {
        titleTable = new JTable(new DefaultTableModel(new Object[]{"ID", "名称"}, 0));
        return titleTable;
    }

    private JPanel createDictionaryColumn(String title, String fieldLabel, SqlConsumer<String> saveAction, JTable table) {
        JTextField nameField = new JTextField(15);
        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> {
            if (nameField.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "请输入" + fieldLabel, "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            safeRun(() -> saveAction.accept(nameField.getText().trim()));
            nameField.setText("");
            refreshDictionaries();
        });

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel(fieldLabel));
        form.add(nameField);
        form.add(saveButton);

        JPanel column = new JPanel(new BorderLayout());
        column.setBorder(new TitledBorder(title));
        column.add(form, BorderLayout.NORTH);
        column.add(new JScrollPane(table), BorderLayout.CENTER);
        return column;
    }

    private JPanel createStaffTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 4, 8, 8));
        JTextField staffCodeField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"M", "F", "O"});
        statusCombo = new JComboBox<>(new String[]{"ACTIVE", "LEAVE", "INACTIVE"});
        departmentCombo = new JComboBox<>();
        positionCombo = new JComboBox<>();
        titleCombo = new JComboBox<>();
        JTextField hireDateField = new JTextField("2025-01-01");

        form.add(new JLabel("工号/编号"));
        form.add(staffCodeField);
        form.add(new JLabel("姓名"));
        form.add(nameField);
        form.add(new JLabel("邮箱"));
        form.add(emailField);
        form.add(new JLabel("电话"));
        form.add(phoneField);
        form.add(new JLabel("性别"));
        form.add(genderCombo);
        form.add(new JLabel("状态"));
        form.add(statusCombo);
        form.add(new JLabel("部门"));
        form.add(departmentCombo);
        form.add(new JLabel("岗位/职务"));
        form.add(positionCombo);
        form.add(new JLabel("职称"));
        form.add(titleCombo);
        form.add(new JLabel("入职日期(yyyy-MM-dd)"));
        form.add(hireDateField);

        JButton saveButton = new JButton("保存/更新教职工");
        saveButton.addActionListener(e -> safeRun(() -> {
            LocalDate hireDate = service.parseDate(hireDateField.getText());
            Staff staff = new Staff(
                    0,
                    staffCodeField.getText().trim(),
                    nameField.getText().trim(),
                    Objects.requireNonNull(genderCombo.getSelectedItem()).toString(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    selectedId(departmentCombo),
                    selectedId(positionCombo),
                    selectedId(titleCombo),
                    Objects.requireNonNull(statusCombo.getSelectedItem()).toString(),
                    hireDate
            );
            service.saveStaff(staff);
            refreshStaffTable();
            JOptionPane.showMessageDialog(this, "教职工信息已保存", "成功", JOptionPane.INFORMATION_MESSAGE);
        }));

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.CENTER);
        top.add(saveButton, BorderLayout.SOUTH);

        staffTable = new JTable(new DefaultTableModel(new Object[]{"ID", "工号", "姓名", "部门", "岗位", "职称", "状态", "邮箱"}, 0));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterDepartmentField = new JTextField(8);
        filterTitleField = new JTextField(8);
        filterPositionField = new JTextField(8);
        filterStatusCombo = new JComboBox<>(new String[]{"", "ACTIVE", "LEAVE", "INACTIVE"});
        JButton searchButton = new JButton("检索");
        searchButton.addActionListener(e -> refreshStaffTable());
        filterPanel.add(new JLabel("按部门"));
        filterPanel.add(filterDepartmentField);
        filterPanel.add(new JLabel("按职称"));
        filterPanel.add(filterTitleField);
        filterPanel.add(new JLabel("按岗位"));
        filterPanel.add(filterPositionField);
        filterPanel.add(new JLabel("状态"));
        filterPanel.add(filterStatusCombo);
        filterPanel.add(searchButton);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(staffTable), BorderLayout.CENTER);
        panel.add(filterPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createRecordTab() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 8, 0));
        panel.add(createEducationPanel());
        panel.add(createFamilyPanel());
        panel.add(createRewardPanel());
        return panel;
    }

    private JPanel createEducationPanel() {
        JPanel eduPanel = new JPanel(new GridLayout(6, 1, 4, 4));
        eduPanel.setBorder(new TitledBorder("学籍/教育经历"));
        JTextField staffCodeField = new JTextField();
        JTextField degreeField = new JTextField();
        JTextField majorField = new JTextField();
        JTextField startDateField = new JTextField("2020-09-01");
        JTextField endDateField = new JTextField("2024-06-30");

        JButton addButton = new JButton("添加教育经历");
        addButton.addActionListener(e -> safeRun(() -> {
            int staffId = requireStaffId(staffCodeField.getText().trim());
            EducationRecord record = new EducationRecord(
                    0,
                    staffId,
                    degreeField.getText().trim(),
                    majorField.getText().trim(),
                    service.parseDate(startDateField.getText()),
                    service.parseDate(endDateField.getText())
            );
            service.addEducation(record);
            JOptionPane.showMessageDialog(this, "教育经历已保存", "成功", JOptionPane.INFORMATION_MESSAGE);
        }));

        eduPanel.add(labeledField("教职工工号", staffCodeField));
        eduPanel.add(labeledField("学历/学位", degreeField));
        eduPanel.add(labeledField("专业", majorField));
        eduPanel.add(labeledField("开始日期", startDateField));
        eduPanel.add(labeledField("结束日期", endDateField));
        eduPanel.add(addButton);
        return eduPanel;
    }

    private JPanel createFamilyPanel() {
        JPanel familyPanel = new JPanel(new GridLayout(6, 1, 4, 4));
        familyPanel.setBorder(new TitledBorder("家庭关系"));
        JTextField staffCodeField = new JTextField();
        JTextField relationField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        JCheckBox accessCheck = new JCheckBox("允许使用教职工信息");

        JButton addButton = new JButton("保存家庭关系");
        addButton.addActionListener(e -> safeRun(() -> {
            int staffId = requireStaffId(staffCodeField.getText().trim());
            FamilyRelation relation = new FamilyRelation(
                    0,
                    staffId,
                    relationField.getText().trim(),
                    nameField.getText().trim(),
                    contactField.getText().trim(),
                    accessCheck.isSelected()
            );
            service.addFamilyRelation(relation);
            JOptionPane.showMessageDialog(this, "家庭信息已保存", "成功", JOptionPane.INFORMATION_MESSAGE);
        }));

        familyPanel.add(labeledField("教职工工号", staffCodeField));
        familyPanel.add(labeledField("关系", relationField));
        familyPanel.add(labeledField("姓名", nameField));
        familyPanel.add(labeledField("联系方式", contactField));
        familyPanel.add(accessCheck);
        familyPanel.add(addButton);
        return familyPanel;
    }

    private JPanel createRewardPanel() {
        JPanel rewardPanel = new JPanel(new GridLayout(6, 1, 4, 4));
        rewardPanel.setBorder(new TitledBorder("奖惩信息"));
        JTextField staffCodeField = new JTextField();
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"REWARD", "PUNISHMENT"});
        JTextField descriptionField = new JTextField();
        JTextField dateField = new JTextField("2024-12-01");

        JButton addButton = new JButton("保存奖惩记录");
        addButton.addActionListener(e -> safeRun(() -> {
            int staffId = requireStaffId(staffCodeField.getText().trim());
            RewardPunishment rp = new RewardPunishment(
                    0,
                    staffId,
                    Objects.requireNonNull(typeCombo.getSelectedItem()).toString(),
                    descriptionField.getText().trim(),
                    service.parseDate(dateField.getText())
            );
            service.addRewardOrPunishment(rp);
            JOptionPane.showMessageDialog(this, "奖惩信息已保存", "成功", JOptionPane.INFORMATION_MESSAGE);
        }));

        rewardPanel.add(labeledField("教职工工号", staffCodeField));
        rewardPanel.add(labeledField("类型", typeCombo));
        rewardPanel.add(labeledField("描述", descriptionField));
        rewardPanel.add(labeledField("日期", dateField));
        rewardPanel.add(addButton);
        return rewardPanel;
    }

    private JPanel createReportTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField deptFilter = new JTextField(10);
        JTextField titleFilter = new JTextField(10);
        JTextField positionFilter = new JTextField(10);
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"", "ACTIVE", "LEAVE", "INACTIVE"});
        JButton searchButton = new JButton("执行查询");
        searchButton.addActionListener(e -> {
            filterDepartmentField.setText(deptFilter.getText());
            filterTitleField.setText(titleFilter.getText());
            filterPositionField.setText(positionFilter.getText());
            filterStatusCombo.setSelectedItem(statusFilter.getSelectedItem());
            refreshStaffTable();
        });

        filters.add(new JLabel("部门"));
        filters.add(deptFilter);
        filters.add(new JLabel("职称"));
        filters.add(titleFilter);
        filters.add(new JLabel("岗位"));
        filters.add(positionFilter);
        filters.add(new JLabel("状态"));
        filters.add(statusFilter);
        filters.add(searchButton);

        reportTable = new JTable(new DefaultTableModel(new Object[]{"部门", "职称", "人数"}, 0));

        panel.add(filters, BorderLayout.NORTH);
        panel.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel labeledField(String label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(label), BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private Integer selectedId(JComboBox<?> comboBox) {
        Object value = comboBox.getSelectedItem();
        if (value instanceof Department department) {
            return department.id();
        }
        if (value instanceof Position position) {
            return position.id();
        }
        if (value instanceof TitleInfo title) {
            return title.id();
        }
        return null;
    }

    private void refreshDictionaries() {
        safeRun(() -> {
            List<Department> departments = service.listDepartments();
            List<Position> positions = service.listPositions();
            List<TitleInfo> titles = service.listTitles();
            updateCombo(departmentCombo, departments.toArray());
            updateCombo(positionCombo, positions.toArray());
            updateCombo(titleCombo, titles.toArray());
            updateTable((DefaultTableModel) departmentTable.getModel(), departments.stream()
                    .map(d -> new Object[]{d.id(), d.name(), d.headcount()})
                    .toList());
            updateTable((DefaultTableModel) positionTable.getModel(), positions.stream()
                    .map(p -> new Object[]{p.id(), p.name()})
                    .toList());
            updateTable((DefaultTableModel) titleTable.getModel(), titles.stream()
                    .map(t -> new Object[]{t.id(), t.name()})
                    .toList());
        });
    }

    private void refreshStaffTable() {
        safeRun(() -> {
            List<StaffView> staff = service.searchStaff(
                    filterDepartmentField != null ? filterDepartmentField.getText() : "",
                    filterTitleField != null ? filterTitleField.getText() : "",
                    filterPositionField != null ? filterPositionField.getText() : "",
                    filterStatusCombo != null && filterStatusCombo.getSelectedItem() != null
                            ? filterStatusCombo.getSelectedItem().toString()
                            : ""
            );
            updateTable((DefaultTableModel) staffTable.getModel(), staff.stream()
                    .map(s -> new Object[]{s.id(), s.staffCode(), s.fullName(), s.department(), s.position(), s.title(), s.status(), s.email()})
                    .toList());
        });
    }

    private void refreshReportTable() {
        safeRun(() -> {
            List<ReportRow> rows = service.departmentTitleReport();
            updateTable((DefaultTableModel) reportTable.getModel(), rows.stream()
                    .map(r -> new Object[]{r.department(), r.title(), r.count()})
                    .toList());
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void updateCombo(JComboBox<?> comboBox, Object[] items) {
        if (comboBox == null) {
            return;
        }
        DefaultComboBoxModel model = new DefaultComboBoxModel(items);
        comboBox.setModel(model);
        if (items.length > 0) {
            comboBox.setSelectedIndex(0);
        }
    }

    private void updateTable(DefaultTableModel model, List<Object[]> rows) {
        model.setRowCount(0);
        for (Object[] row : rows) {
            model.addRow(row);
        }
    }

    private void safeRun(SqlRunnable runnable) {
        try {
            runnable.run();
        } catch (SQLException ex) {
            if (GraphicsEnvironment.isHeadless()) {
                System.err.println("数据库错误: " + ex.getMessage());
            } else {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "数据库错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RuntimeException ex) {
            if (GraphicsEnvironment.isHeadless()) {
                System.err.println("数据处理异常: " + ex.getMessage());
            } else {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "数据处理异常", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private int requireStaffId(String staffCode) throws SQLException {
        return service.findStaffIdByCode(staffCode)
                .orElseThrow(() -> new SQLException("未找到工号为 " + staffCode + " 的教职工"));
    }

    @FunctionalInterface
    private interface SqlRunnable {
        void run() throws SQLException;
    }

    @FunctionalInterface
    private interface SqlConsumer<T> {
        void accept(T value) throws SQLException;
    }
}
