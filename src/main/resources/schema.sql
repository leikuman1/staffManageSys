-- staff management schema with 045 suffix
CREATE DATABASE IF NOT EXISTS staffdb045 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE staffdb045;

CREATE TABLE IF NOT EXISTS department045 (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  headcount INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS position045 (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS title045 (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS staff045 (
  id INT AUTO_INCREMENT PRIMARY KEY,
  staff_code VARCHAR(30) NOT NULL UNIQUE,
  full_name VARCHAR(120) NOT NULL,
  gender ENUM('M','F','O') NOT NULL,
  email VARCHAR(200) NOT NULL,
  phone VARCHAR(50),
  department_id INT NOT NULL,
  position_id INT,
  title_id INT,
  status ENUM('ACTIVE','LEAVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  hire_date DATE,
  leave_date DATE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT email_format_045 CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$'),
  CONSTRAINT fk_staff_department_045 FOREIGN KEY (department_id) REFERENCES department045(id),
  CONSTRAINT fk_staff_position_045 FOREIGN KEY (position_id) REFERENCES position045(id),
  CONSTRAINT fk_staff_title_045 FOREIGN KEY (title_id) REFERENCES title045(id)
);

CREATE TABLE IF NOT EXISTS education045 (
  id INT AUTO_INCREMENT PRIMARY KEY,
  staff_id INT NOT NULL,
  degree VARCHAR(120) NOT NULL,
  major VARCHAR(120) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  CONSTRAINT chk_education_dates_045 CHECK (start_date <= end_date),
  CONSTRAINT fk_education_staff_045 FOREIGN KEY (staff_id) REFERENCES staff045(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS family045 (
  id INT AUTO_INCREMENT PRIMARY KEY,
  staff_id INT NOT NULL,
  relation VARCHAR(50) NOT NULL,
  name VARCHAR(100) NOT NULL,
  contact VARCHAR(100),
  can_access BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_family_staff_045 FOREIGN KEY (staff_id) REFERENCES staff045(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reward_punishment045 (
  id INT AUTO_INCREMENT PRIMARY KEY,
  staff_id INT NOT NULL,
  type ENUM('REWARD','PUNISHMENT') NOT NULL,
  description VARCHAR(500) NOT NULL,
  occur_date DATE NOT NULL,
  CONSTRAINT fk_reward_staff_045 FOREIGN KEY (staff_id) REFERENCES staff045(id) ON DELETE CASCADE
);

-- Trigger to maintain department headcount after insert
CREATE TRIGGER trg_staff_department_inc045 AFTER INSERT ON staff045
FOR EACH ROW
UPDATE department045 SET headcount = headcount + 1 WHERE id = NEW.department_id;

-- Trigger to maintain department headcount after delete
CREATE TRIGGER trg_staff_department_dec045 AFTER DELETE ON staff045
FOR EACH ROW
UPDATE department045 SET headcount = headcount - 1 WHERE id = OLD.department_id AND headcount > 0;

-- Trigger to adjust headcount when department changes
CREATE TRIGGER trg_staff_department_update045 AFTER UPDATE ON staff045
FOR EACH ROW
UPDATE department045 d
SET headcount = headcount + CASE WHEN d.id = NEW.department_id THEN 1 ELSE 0 END
                  + CASE WHEN d.id = OLD.department_id THEN -1 ELSE 0 END
WHERE d.id IN (OLD.department_id, NEW.department_id);

DROP PROCEDURE IF EXISTS sp_department_title_counts045;
CREATE PROCEDURE sp_department_title_counts045()
    READS SQL DATA
    SELECT d.name AS department, t.name AS title, COUNT(s.id) AS staff_count
    FROM department045 d
    LEFT JOIN staff045 s ON s.department_id = d.id
    LEFT JOIN title045 t ON s.title_id = t.id
    GROUP BY d.name, t.name
    ORDER BY d.name, t.name;

-- Seed basic dictionaries for quick start
INSERT INTO department045(name) VALUES ('教务处'), ('信息中心'), ('人事处')
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO position045(name) VALUES ('教师'), ('行政'), ('教研员')
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO title045(name) VALUES ('助教'), ('讲师'), ('副教授'), ('教授')
ON DUPLICATE KEY UPDATE name=VALUES(name);
