-- ============================================================
-- College DBMS / JDBC Lab Assessment
-- Database Setup Script: college_db
-- ============================================================

-- Step 1: Create the database if it doesn't already exist
CREATE DATABASE IF NOT EXISTS college_db;

-- Step 2: Switch to using the college_db database
USE college_db;

-- Step 3: Create the students table
-- student_id : Primary Key (unique identifier for each student)
-- name       : Full student name
-- department : Academic department
-- email      : Valid contact email address
CREATE TABLE IF NOT EXISTS students (
    student_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL
);

-- Step 4: Clear existing sample rows (optional for fresh start)
-- TRUNCATE TABLE students;

-- Step 5: Insert sample student records for testing CRUD operations
INSERT INTO students (student_id, name, department, email) VALUES
(101, 'Aarav Sharma', 'Computer Science and Engineering', 'aarav.sharma@college.edu'),
(102, 'Priya Patel', 'Information Technology', 'priya.patel@college.edu'),
(103, 'Rohan Verma', 'Electronics and Communication', 'rohan.verma@college.edu'),
(104, 'Sneha Reddy', 'Computer Science and Engineering', 'sneha.reddy@college.edu'),
(105, 'Vikram Singh', 'Mechanical Engineering', 'vikram.singh@college.edu')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name), 
    department = VALUES(department), 
    email = VALUES(email);

-- Verify the records
SELECT * FROM students;
