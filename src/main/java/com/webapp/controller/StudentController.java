package com.webapp.controller;

import com.webapp.entity.Student;
import com.webapp.repository.StudentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentController {
    private final StudentRepository studentRepo;

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
        return "student_form";
    }

    @PostMapping("/save")
    public String saveStudent(@Valid @ModelAttribute Student student, BindingResult result) {
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
        return "student_form";
    }

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentRepo.deleteById(id);
        return "redirect:/students";
    }
}
