/**
 * Student Management System - Frontend Application Logic
 * Vanilla JavaScript communicating with Java Servlets backend via RESTful JSON APIs.
 */

// API Base Endpoint: relative URL ensures compatibility with any Tomcat context path
const API_BASE = "api/students";

// Global timer reference for alert banner auto-dismiss
let alertTimer = null;

// =========================================================================
// INITIALIZATION
// =========================================================================
document.addEventListener("DOMContentLoaded", () => {
    // Automatically fetch and display all existing students when page loads
    loadStudents();
});

// =========================================================================
// 1. READ OPERATION: Fetch and display all students
// =========================================================================
async function loadStudents() {
    const tableBody = document.getElementById("studentsTableBody");
    const emptyState = document.getElementById("emptyState");

    try {
        tableBody.innerHTML = `
            <tr>
                <td colspan="5" class="table-status">Loading student records from MySQL database...</td>
            </tr>
        `;

        const response = await fetch(API_BASE);

        if (!response.ok) {
            throw new Error(`Failed to load students. Server responded with status ${response.status}`);
        }

        const students = await response.json();

        // Clear loading message
        tableBody.innerHTML = "";

        if (!students || students.length === 0) {
            emptyState.classList.remove("hidden");
            return;
        }

        emptyState.classList.add("hidden");

        // Populate table rows dynamically
        students.forEach(student => {
            const tr = document.createElement("tr");

            // Encode values safely to prevent any injection
            const safeStudent = {
                studentId: student.studentId,
                name: escapeHtml(student.name),
                department: escapeHtml(student.department),
                email: escapeHtml(student.email)
            };

            tr.innerHTML = `
                <td class="student-id-cell">${safeStudent.studentId}</td>
                <td><strong>${safeStudent.name}</strong></td>
                <td><span class="dept-badge">${safeStudent.department}</span></td>
                <td>${safeStudent.email}</td>
                <td class="text-center">
                    <button type="button" class="btn btn-sm btn-edit" 
                        onclick="openEditModal(${safeStudent.studentId}, '${escapeJsString(student.name)}', '${escapeJsString(student.department)}', '${escapeJsString(student.email)}')">
                        Edit
                    </button>
                    <button type="button" class="btn btn-sm btn-delete" 
                        onclick="handleDeleteStudent(${safeStudent.studentId}, '${escapeJsString(student.name)}')">
                        Delete
                    </button>
                </td>
            `;

            tableBody.appendChild(tr);
        });

    } catch (error) {
        console.error("Error in loadStudents:", error);
        tableBody.innerHTML = `
            <tr>
                <td colspan="5" class="table-status" style="color: var(--danger-color);">
                    Unable to load student records. Please verify Tomcat is running and MySQL database is connected.
                </td>
            </tr>
        `;
        showAlert("Failed to connect to the backend server or database.", "error");
    }
}

// =========================================================================
// 2. CREATE OPERATION: Add a new student
// =========================================================================
async function handleAddStudent(event) {
    event.preventDefault();

    const studentIdInput = document.getElementById("studentId");
    const nameInput = document.getElementById("name");
    const deptInput = document.getElementById("department");
    const emailInput = document.getElementById("email");
    const addBtn = document.getElementById("addBtn");

    const studentId = parseInt(studentIdInput.value.trim(), 10);
    const name = nameInput.value.trim();
    const department = deptInput.value.trim();
    const email = emailInput.value.trim();

    if (!studentId || studentId <= 0 || !name || !department || !email) {
        showAlert("Please fill in all required fields with valid values.", "error");
        return;
    }

    const payload = {
        studentId: studentId,
        name: name,
        department: department,
        email: email
    };

    try {
        addBtn.disabled = true;
        addBtn.innerText = "Adding...";

        const response = await fetch(API_BASE, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        const result = await response.json();

        if (response.ok && result.success) {
            showAlert(result.message || "Student added successfully!", "success");
            // Reset form
            document.getElementById("addStudentForm").reset();
            // Refresh table to display newly added record
            loadStudents();
        } else {
            showAlert(result.message || "Failed to add student.", "error");
        }

    } catch (error) {
        console.error("Error in handleAddStudent:", error);
        showAlert("Network error occurred while adding student.", "error");
    } finally {
        addBtn.disabled = false;
        addBtn.innerHTML = '<span class="btn-icon">&#43;</span> Add Student';
    }
}

// =========================================================================
// 3. UPDATE OPERATION: Open modal and save updated student
// =========================================================================
function openEditModal(studentId, name, department, email) {
    // Populate form with existing details
    document.getElementById("editStudentId").value = studentId;
    document.getElementById("editName").value = name;
    document.getElementById("editDepartment").value = department;
    document.getElementById("editEmail").value = email;

    // Display modal dialog
    document.getElementById("editModal").classList.remove("hidden");
}

function closeEditModal() {
    document.getElementById("editModal").classList.add("hidden");
}

async function handleUpdateStudent(event) {
    event.preventDefault();

    const studentId = parseInt(document.getElementById("editStudentId").value, 10);
    const name = document.getElementById("editName").value.trim();
    const department = document.getElementById("editDepartment").value.trim();
    const email = document.getElementById("editEmail").value.trim();
    const saveBtn = document.getElementById("saveUpdateBtn");

    if (!name || !department || !email) {
        showAlert("Name, department, and email are required fields.", "error");
        return;
    }

    const payload = {
        studentId: studentId,
        name: name,
        department: department,
        email: email
    };

    try {
        saveBtn.disabled = true;
        saveBtn.innerText = "Saving...";

        // PUT request to /api/students/{id}
        const response = await fetch(`${API_BASE}/${studentId}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        const result = await response.json();

        if (response.ok && result.success) {
            showAlert(result.message || "Student updated successfully!", "success");
            closeEditModal();
            loadStudents();
        } else {
            showAlert(result.message || "Failed to update student.", "error");
        }

    } catch (error) {
        console.error("Error in handleUpdateStudent:", error);
        showAlert("Network error occurred while updating student.", "error");
    } finally {
        saveBtn.disabled = false;
        saveBtn.innerText = "Save Changes";
    }
}

// =========================================================================
// 4. DELETE OPERATION: Confirm and delete student
// =========================================================================
async function handleDeleteStudent(studentId, studentName) {
    // Confirmation dialog before deleting
    const confirmed = window.confirm(`Are you sure you want to delete student "${studentName}" (ID: ${studentId})?\nThis action cannot be undone.`);

    if (!confirmed) {
        return;
    }

    try {
        // DELETE request to /api/students/{id}
        const response = await fetch(`${API_BASE}/${studentId}`, {
            method: "DELETE"
        });

        const result = await response.json();

        if (response.ok && result.success) {
            showAlert(result.message || "Student deleted successfully!", "success");
            loadStudents();
        } else {
            showAlert(result.message || "Failed to delete student.", "error");
        }

    } catch (error) {
        console.error("Error in handleDeleteStudent:", error);
        showAlert("Network error occurred while deleting student.", "error");
    }
}

// =========================================================================
// NOTIFICATION & UI UTILITIES
// =========================================================================
function showAlert(message, type = "success") {
    const alertBox = document.getElementById("alertBox");
    const alertMessage = document.getElementById("alertMessage");

    // Clear any previous auto-dismiss timer
    if (alertTimer) {
        clearTimeout(alertTimer);
    }

    alertMessage.innerText = message;
    alertBox.className = `alert alert-${type}`;
    alertBox.classList.remove("hidden");

    // Scroll to top smoothly so message is clearly visible
    window.scrollTo({ top: 0, behavior: "smooth" });

    // Auto dismiss after 4.5 seconds
    alertTimer = setTimeout(() => {
        closeAlert();
    }, 4500);
}

function closeAlert() {
    const alertBox = document.getElementById("alertBox");
    alertBox.classList.add("hidden");
}

// Utility: Prevent XSS when inserting dynamic text into HTML
function escapeHtml(str) {
    if (!str) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// Utility: Escape single quotes for inline JS function arguments
function escapeJsString(str) {
    if (!str) return "";
    return String(str).replace(/'/g, "\\'");
}
