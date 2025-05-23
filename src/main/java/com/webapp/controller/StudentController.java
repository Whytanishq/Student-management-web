package com.webapp.controller;

import com.webapp.entity.Student;
import com.webapp.repository.StudentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentRepository studentRepo;

    private static final Map<String, String> DEPARTMENTS;
    static {
        DEPARTMENTS = new LinkedHashMap<>();
        DEPARTMENTS.put("CSE", "Computer Science and Engineering");
        DEPARTMENTS.put("ECE", "Electronics and Communication Engineering");
        DEPARTMENTS.put("EEE", "Electrical and Electronics Engineering");
        DEPARTMENTS.put("MEC", "Mechanical Engineering");
        DEPARTMENTS.put("CIV", "Civil Engineering");
        DEPARTMENTS.put("INF", "Information Technology");
        DEPARTMENTS.put("AID", "Artificial Intelligence and Data Science");
        DEPARTMENTS.put("DSC", "Data Science");
    }

    public StudentController(StudentRepository studentRepo) {
        this.studentRepo = studentRepo;
    }

    @GetMapping
    public String listStudents(Model model) {
        List<Student> students = studentRepo.findAll();
        model.addAttribute("students", students);
        return "student_list";
    }

    @GetMapping("/new")
    public String newStudentForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("departments", DEPARTMENTS);
        return "student_form";
    }

    @PostMapping("/save")
    public String saveStudent(@Valid @ModelAttribute Student student, BindingResult result, Model model) {
        model.addAttribute("departments", DEPARTMENTS);

        boolean emailExists = studentRepo.existsByEmail(student.getEmail());
        if (emailExists) {
            if (student.getId() == null || !studentRepo.findById(student.getId())
                    .map(s -> s.getEmail().equals(student.getEmail())).orElse(false)) {
                result.rejectValue("email", "error.student", "This mail has already been used");
            }
        }

        if (student.getId() == null) {
            String deptCode = getDepartmentCode(student.getDepartment()); // 3-letter code
            String year = String.valueOf(Year.now().getValue()).substring(2); // last 2 digits
            String prefix = year + deptCode;
            long count = studentRepo.countByEnrollmentNoStartingWith(prefix);
            String sequence = String.format("%04d", count + 1);
            String enrollmentNo = prefix + sequence;
            while (studentRepo.existsByEnrollmentNo(enrollmentNo)) {
                count++;
                sequence = String.format("%04d", count + 1);
                enrollmentNo = prefix + sequence;
            }
            student.setEnrollmentNo(enrollmentNo);
        } else {
            Student existing = studentRepo.findById(student.getId()).orElse(null);
            if (existing != null) {
                student.setEnrollmentNo(existing.getEnrollmentNo());
            }
        }

        if (result.hasErrors()) {
            return "student_form";
        }

        studentRepo.save(student);
        return "redirect:/students";
    }

    @GetMapping("/edit/{id}")
    public String editStudent(@PathVariable Long id, Model model) {
        Student student = studentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));
        model.addAttribute("student", student);
        model.addAttribute("departments", DEPARTMENTS);
        return "student_form";
    }

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentRepo.deleteById(id);
        return "redirect:/students";
    }

    private String getDepartmentCode(String departmentName) {
        for (Map.Entry<String, String> entry : DEPARTMENTS.entrySet()) {
            if (entry.getValue().equals(departmentName)) {
                return entry.getKey();
            }
        }
        return departmentName.replaceAll("[^A-Z]", "").substring(0, 3);
    }
}
