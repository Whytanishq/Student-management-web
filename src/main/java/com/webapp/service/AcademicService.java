package com.webapp.service;

import com.webapp.entity.*;
import com.webapp.respository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AcademicService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    // Updated GPA calculation logic on 10-point scale
    private static final Map<String, Double> GRADE_POINTS = Map.ofEntries(
            Map.entry("A+", 10.0), Map.entry("A", 9.0), Map.entry("A-", 8.5),
            Map.entry("B+", 8.0), Map.entry("B", 7.0), Map.entry("B-", 6.5),
            Map.entry("C+", 6.0), Map.entry("C", 5.5), Map.entry("C-", 5.0),
            Map.entry("D+", 4.5), Map.entry("D", 4.0), Map.entry("F", 0.0)
    );

    public double calculateGPA(List<Enrollment> enrollments) {
        double totalPoints = 0.0;
        int totalCredits = 0;
        for (Enrollment e : enrollments) {
            Double gradePoint = GRADE_POINTS.getOrDefault(e.getGrade(), 0.0);
            int credits = e.getSubject().getCredits();
            totalPoints += gradePoint * credits;
            totalCredits += credits;
        }
        return totalCredits == 0 ? 0.0 : totalPoints / totalCredits;
    }

    public boolean isPass(List<Enrollment> enrollments) {
        for (Enrollment e : enrollments) {
            if ("F".equalsIgnoreCase(e.getGrade())) return false;
        }
        return true;
    }

    public List<Enrollment> getCurrentSemesterEnrollments(Student student) {
        List<Enrollment> all = enrollmentRepository.findByStudentOrderBySemesterIdDesc(student);
        if (all.isEmpty()) return Collections.emptyList();
        Semester current = all.get(0).getSemester();
        return enrollmentRepository.findByStudentAndSemesterOrderBySubjectId(student, current);
    }

    public Map<Semester, List<Enrollment>> getAcademicHistory(Student student) {
        List<Enrollment> all = enrollmentRepository.findByStudent(student);
        Map<Semester, List<Enrollment>> history = new LinkedHashMap<>();
        for (Enrollment e : all) {
            history.computeIfAbsent(e.getSemester(), k -> new ArrayList<>()).add(e);
        }
        return history;
    }
}
