package com.college;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentDAO (Data Access Object)
 * 
 * Handles all CRUD (Create, Read, Update, Delete) database interactions
 * with the MySQL 'students' table using JDBC PreparedStatement to prevent SQL Injection.
 */
public class StudentDAO {

    /**
     * READ: Retrieves all student records ordered by student_id.
     * 
     * @return List of Student objects
     * @throws SQLException if a database access error occurs
     */
    public List<Student> getAllStudents() throws SQLException {
        List<Student> studentList = new ArrayList<>();
        String sql = "SELECT student_id, name, department, email FROM students ORDER BY student_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("student_id");
                String name = rs.getString("name");
                String department = rs.getString("department");
                String email = rs.getString("email");

                studentList.add(new Student(id, name, department, email));
            }
        }
        return studentList;
    }

    /**
     * READ: Retrieves a single student record by their primary key (student_id).
     * 
     * @param studentId the ID of the student to fetch
     * @return Student object if found, or null if not found
     * @throws SQLException if a database access error occurs
     */
    public Student getStudentById(int studentId) throws SQLException {
        String sql = "SELECT student_id, name, department, email FROM students WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getInt("student_id"),
                            rs.getString("name"),
                            rs.getString("department"),
                            rs.getString("email")
                    );
                }
            }
        }
        return null;
    }

    /**
     * CREATE: Inserts a new student record into the MySQL database.
     * 
     * @param student the Student object containing new details
     * @return true if insertion was successful (1 row affected)
     * @throws SQLException if a database access error occurs or duplicate primary key
     */
    public boolean addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (student_id, name, department, email) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, student.getStudentId());
            stmt.setString(2, student.getName());
            stmt.setString(3, student.getDepartment());
            stmt.setString(4, student.getEmail());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * UPDATE: Updates the name, department, and email of an existing student.
     * The student_id remains unchanged as it is part of the WHERE condition.
     * 
     * @param student the Student object with updated details
     * @return true if update was successful (1 row affected)
     * @throws SQLException if a database access error occurs
     */
    public boolean updateStudent(Student student) throws SQLException {
        String sql = "UPDATE students SET name = ?, department = ?, email = ? WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student.getName());
            stmt.setString(2, student.getDepartment());
            stmt.setString(3, student.getEmail());
            stmt.setInt(4, student.getStudentId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * DELETE: Removes a student record from the database by student_id.
     * 
     * @param studentId the ID of the student to remove
     * @return true if deletion was successful (1 row affected)
     * @throws SQLException if a database access error occurs
     */
    public boolean deleteStudent(int studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Helper check: Determines whether a student with the specified ID already exists.
     * 
     * @param studentId the ID to check
     * @return true if student exists, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean existsById(int studentId) throws SQLException {
        String sql = "SELECT 1 FROM students WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
