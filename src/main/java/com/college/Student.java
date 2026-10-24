package com.college;

import java.io.Serializable;

/**
 * Student Model / Entity Class (JavaBean / POJO)
 * 
 * Represents a student record matching the 'students' table in MySQL database.
 */
public class Student implements Serializable {
    private static final long serialVersionUID = 1L;

    private int studentId;
    private String name;
    private String department;
    private String email;

    // Default No-Arg Constructor (required for JSON serialization / JavaBeans)
    public Student() {
    }

    // Parameterized Constructor
    public Student(int studentId, String name, String department, String email) {
        this.studentId = studentId;
        this.name = name;
        this.department = department;
        this.email = email;
    }

    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
