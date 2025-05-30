package com.webapp.controller;

import com.webapp.entity.*;
import com.webapp.respository.StudentRepository;
import com.webapp.service.AcademicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class AcademicController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AcademicService academicService;

    @GetMapping("/student/details")
    public String studentDetails(Model model, Authentication authentication) {
        String enrollmentNo = authentication.getName();
        Student student = studentRepository.findByEnrollmentNo(enrollmentNo)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<Enrollment> currentEnrollments = academicService.getCurrentSemesterEnrollments(student);
        double currentGPA = academicService.calculateGPA(currentEnrollments);
        boolean isPass = academicService.isPass(currentEnrollments);

        model.addAttribute("student", student);
        model.addAttribute("currentEnrollments", currentEnrollments);
        model.addAttribute("currentGPA", currentGPA);
        model.addAttribute("isPass", isPass);

        return "student_details";
    }

    @GetMapping("/student/academic-history")
    public String academicHistory(Model model, Authentication authentication) {
        try {
            String enrollmentNo = authentication.getName();
            Student student = studentRepository.findByEnrollmentNo(enrollmentNo)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found"));

            Map<Semester, List<Enrollment>> history = academicService.getAcademicHistory(student);

            model.addAttribute("student", student);
            model.addAttribute("history", history);

            return "academic_history";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
}