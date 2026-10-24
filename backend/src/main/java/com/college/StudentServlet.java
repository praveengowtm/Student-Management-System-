package com.college;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

/**
 * StudentServlet
 * 
 * RESTful Controller Servlet mapped to /api/students and /api/students/*
 * Handles HTTP GET, POST, PUT, DELETE requests and communicates via JSON.
 */
@WebServlet(name = "StudentServlet", urlPatterns = {"/api/students", "/api/students/*"})
public class StudentServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();
    private final Gson gson = new Gson();

    /**
     * GET /api/students       - Returns all students as a JSON array
     * GET /api/students/{id}  - Returns a single student by ID
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        setupResponseHeaders(response);
        Integer studentId = extractStudentId(request);

        try {
            if (studentId == null) {
                // Fetch all students
                List<Student> students = studentDAO.getAllStudents();
                sendJsonResponse(response, HttpServletResponse.SC_OK, students);
            } else {
                // Fetch a single student by ID
                Student student = studentDAO.getStudentById(studentId);
                if (student != null) {
                    sendJsonResponse(response, HttpServletResponse.SC_OK, student);
                } else {
                    sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                            "Student with ID " + studentId + " not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Database error while retrieving students: " + e.getMessage());
        }
    }

    /**
     * POST /api/students - Creates a new student record
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        setupResponseHeaders(response);

        try {
            String requestBody = readRequestBody(request);
            if (requestBody == null || requestBody.trim().isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Request body cannot be empty.");
                return;
            }

            Student student = gson.fromJson(requestBody, Student.class);

            // Validation
            if (student == null || student.getStudentId() <= 0) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "A valid positive Student ID is required.");
                return;
            }
            if (student.getName() == null || student.getName().trim().isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Student Name is required.");
                return;
            }
            if (student.getDepartment() == null || student.getDepartment().trim().isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Department is required.");
                return;
            }
            if (student.getEmail() == null || student.getEmail().trim().isEmpty() || !student.getEmail().contains("@")) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "A valid email address is required.");
                return;
            }

            // Check if student with this ID already exists
            if (studentDAO.existsById(student.getStudentId())) {
                sendErrorResponse(response, HttpServletResponse.SC_CONFLICT, 
                        "Student with ID " + student.getStudentId() + " already exists.");
                return;
            }

            // Add student via DAO
            boolean success = studentDAO.addStudent(student);
            if (success) {
                sendSuccessResponse(response, HttpServletResponse.SC_CREATED, "Student added successfully.");
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to insert student record.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request payload.");
        }
    }

    /**
     * PUT /api/students/{id} - Updates an existing student record.
     * Note: The student_id cannot be changed during an update.
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        setupResponseHeaders(response);
        Integer pathStudentId = extractStudentId(request);

        try {
            String requestBody = readRequestBody(request);
            if (requestBody == null || requestBody.trim().isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Request body cannot be empty.");
                return;
            }

            Student student = gson.fromJson(requestBody, Student.class);
            if (student == null) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid student data.");
                return;
            }

            // Determine effective student ID from URL path or body
            int effectiveId = (pathStudentId != null) ? pathStudentId : student.getStudentId();
            if (effectiveId <= 0) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "A valid Student ID is required for update.");
                return;
            }
            // Enforce that student_id cannot be modified
            student.setStudentId(effectiveId);

            // Validation
            if (student.getName() == null || student.getName().trim().isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Student Name is required.");
                return;
            }
            if (student.getDepartment() == null || student.getDepartment().trim().isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Department is required.");
                return;
            }
            if (student.getEmail() == null || student.getEmail().trim().isEmpty() || !student.getEmail().contains("@")) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "A valid email address is required.");
                return;
            }

            // Verify existence
            if (!studentDAO.existsById(effectiveId)) {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                        "Student with ID " + effectiveId + " does not exist.");
                return;
            }

            // Perform update via DAO
            boolean success = studentDAO.updateStudent(student);
            if (success) {
                sendSuccessResponse(response, HttpServletResponse.SC_OK, "Student updated successfully.");
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update student record.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request payload.");
        }
    }

    /**
     * DELETE /api/students/{id} - Deletes a student record by ID
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        setupResponseHeaders(response);
        Integer studentId = extractStudentId(request);

        if (studentId == null || studentId <= 0) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Valid Student ID in URL path is required.");
            return;
        }

        try {
            if (!studentDAO.existsById(studentId)) {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                        "Student with ID " + studentId + " not found.");
                return;
            }

            boolean success = studentDAO.deleteStudent(studentId);
            if (success) {
                sendSuccessResponse(response, HttpServletResponse.SC_OK, "Student deleted successfully.");
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete student record.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Database error: " + e.getMessage());
        }
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private void setupResponseHeaders(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    private Integer extractStudentId(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.trim().isEmpty() || pathInfo.equals("/")) {
            return null;
        }
        String[] parts = pathInfo.split("/");
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                try {
                    return Integer.parseInt(part.trim());
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private void sendJsonResponse(HttpServletResponse response, int statusCode, Object data) throws IOException {
        response.setStatus(statusCode);
        PrintWriter writer = response.getWriter();
        writer.print(gson.toJson(data));
        writer.flush();
    }

    private void sendSuccessResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("success", true);
        json.addProperty("message", message);
        sendJsonResponse(response, statusCode, json);
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("success", false);
        json.addProperty("message", message);
        sendJsonResponse(response, statusCode, json);
    }
}
