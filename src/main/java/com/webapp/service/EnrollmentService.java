package com.webapp.service;

import com.webapp.entity.*;
import com.webapp.respository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

@Service
public class EnrollmentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private final Random random = new Random();

    @Transactional
    public void enrollAllStudents() {
        List<Student> students = studentRepository.findAll();
        for (Student student : students) {
            enrollStudent(student);
        }
    }

    @Transactional
    public void enrollStudent(Student student) {
        // Clear any existing enrollments for this student
        enrollmentRepository.deleteByStudentId(student.getId());

        int currentSemester = calculateCurrentSemesterNumber(student);
        String departmentCode = getDepartmentCode(student.getDepartment());

        for (int sem = 1; sem <= currentSemester; sem++) {
            String semesterName = "Semester " + sem;
            Semester semester = (Semester) semesterRepository.findByName(semesterName)
                    .orElseThrow(() -> new RuntimeException("Semester not found: " + semesterName));

            List<Subject> subjects = subjectRepository.findBySemesterAndDepartment(semesterName, departmentCode);

            for (Subject subject : subjects) {
                Enrollment enrollment = new Enrollment();
                enrollment.setStudent(student);
                enrollment.setSemester(semester);
                enrollment.setSubject(subject);

                if (sem < currentSemester) {
                    // For passed semesters, generate random marks (40-100)
                    int marks = 40 + random.nextInt(61);
                    enrollment.setMarks(marks);
                    enrollment.setGrade(calculateGrade(marks));
                    enrollment.setPassed(true);
                } else {
                    // For current semester, initialize with 0 marks
                    enrollment.setMarks(0);
                    enrollment.setGrade("F");
                    enrollment.setPassed(false);
                }

                enrollmentRepository.save(enrollment);
            }
        }
    }

    private int calculateCurrentSemesterNumber(Student student) {
        try {
            Calendar now = Calendar.getInstance();
            int currentYear = now.get(Calendar.YEAR);
            int currentMonth = now.get(Calendar.MONTH) + 1; // January is 0

            String enrollmentNo = student.getEnrollmentNo();
            if (enrollmentNo == null || enrollmentNo.length() < 2) {
                return 1; // Default to first semester
            }

            int admissionYear = 2000 + Integer.parseInt(enrollmentNo.substring(0, 2));
            int academicYear = currentYear - admissionYear;

            // Semester logic:
            // June-November: Odd semester (1,3,5,7)
            // December-May: Even semester (2,4,6,8)
            if (currentMonth >= 6 && currentMonth <= 11) {
                // Odd semester
                return academicYear * 2 + 1;
            } else {
                // Even semester (December counts for next academic year)
                if (currentMonth == 12) academicYear++;
                return academicYear * 2;
            }
        } catch (Exception e) {
            return 1; // Fallback to first semester
        }
    }

    private String calculateGrade(int marks) {
        if (marks >= 90) return "A+";
        if (marks >= 80) return "A";
        if (marks >= 70) return "B+";
        if (marks >= 60) return "B";
        if (marks >= 50) return "C";
        if (marks >= 40) return "D";
        return "F";
    }

    private String getDepartmentCode(String departmentName) {
        if (departmentName.contains("Computer Science")) return "CSE";
        if (departmentName.contains("Electronics and Communication")) return "ECE";
        if (departmentName.contains("Electrical and Electronics")) return "EEE";
        if (departmentName.contains("Mechanical")) return "MEC";
        if (departmentName.contains("Civil")) return "CIV";
        if (departmentName.contains("Information Technology")) return "INF";
        if (departmentName.contains("Artificial Intelligence")) return "AID";
        if (departmentName.contains("Data Science")) return "DSC";
        return "GEN";
    }
}