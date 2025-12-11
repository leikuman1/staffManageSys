# staffManageSys

基于 Java Swing（JDK 17）的教职工管理示例，涵盖组织主数据（部门、职务/岗位、职称）、教职工、学籍/教育经历、家庭关系、奖惩记录以及按部门 × 职称的人数统计。

## 功能要点
- **数据库后缀 045**：数据库 `staffdb045` 以及所有表、触发器、存储过程均带 045 后缀。
- **统一字典**：部门、岗位/职务、职称维护。
- **教职工管理**：基本信息、组织属性、在职状态，邮箱格式校验（CHECK），部门人数自动维护（INSERT/DELETE/UPDATE 触发器）。
- **学籍/教育经历**：学历/学位、专业、起止日期校验（CHECK）。
- **家庭关系**：亲属信息与权限标记。
- **奖惩信息**：奖励/惩罚记录。
- **自动化与约束**：存储过程 `sp_department_title_counts045` 汇总部门 × 职称人数；触发器维护部门人数；邮箱格式校验与参照完整性。
- **查询报表**：按部门/职称/岗位/状态检索与统计。

## 运行与构建
1. 确保本地有 MySQL，创建并加载数据库脚本：
   ```bash
   mysql -u<user> -p < src/main/resources/schema.sql
   ```
   或者将 `application.properties` / 环境变量 `DB_URL`、`DB_USER`、`DB_PASSWORD` 配好，并设置 `db.initialize=true` 让程序启动时自动执行脚本。
2. 使用 Maven 编译（需要 JDK 17）：
   ```bash
   mvn clean package
   ```
3. 运行桌面程序：
   ```bash
   java -jar target/staffManageSys-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```
4. 生成界面截图（无须显示窗口）：
   ```bash
   java -jar target/staffManageSys-1.0-SNAPSHOT-jar-with-dependencies.jar --screenshot
   # 生成 ui-preview.png
   ```

## 主要文件
- `src/main/resources/schema.sql`：数据库、表、触发器、存储过程及示例数据。
- `src/main/java/com/school045/StaffManageApplication.java`：程序入口，支持 `--screenshot`。
- `src/main/java/com/school045/ui/StaffMainPanel.java`：Swing 界面（组织主数据、教职工、学籍/家庭/奖惩、查询报表）。
- `src/main/java/com/school045/service/StaffService.java`：JDBC 服务，封装 CRUD、检索和统计调用。
