package com.webapp.controller;

import com.webapp.entity.*;
import com.webapp.respository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.Year;
import java.util.*;

@Controller
public class StudentController {

    private final StudentRepository studentRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

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

    @Autowired
    public StudentController(StudentRepository studentRepo) {
        this.studentRepo = studentRepo;
    }

    // Admin access to student list
    @RequestMapping("/students")
    public String studentManagement(Model model) {
        List<Student> students = studentRepo.findAll();
        model.addAttribute("students", students);
        return "student_list";
    }

    @GetMapping("/students/new")
    public String newStudentForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("departments", DEPARTMENTS);
        return "student_form";
    }

    @PostMapping("/students/save")
    public String saveStudent(@Valid @ModelAttribute Student student, BindingResult result, Model model) {
        model.addAttribute("departments", DEPARTMENTS);

        // Check for email uniqueness
        boolean emailExists = studentRepo.existsByEmail(student.getEmail());
        if (emailExists) {
            if (student.getId() == null || !studentRepo.findById(student.getId())
                    .map(s -> s.getEmail().equals(student.getEmail())).orElse(false)) {
                result.rejectValue("email", "error.student", "This email has already been used");
            }
        }

        // Enrollment number generation for new students
        if (student.getId() == null) {
            String deptCode = getDepartmentCode(student.getDepartment());
            String year = String.valueOf(Year.now().getValue()).substring(2);
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
            // Preserve existing enrollment number
            Student existing = studentRepo.findById(student.getId()).orElse(null);
            if (existing != null) {
                student.setEnrollmentNo(existing.getEnrollmentNo());
            }
        }

        if (result.hasErrors()) {
            return "student_form";
        }

        studentRepo.save(student);
        createOrUpdateStudentUserAccount(student);

        return "redirect:/students";
    }

    @GetMapping("/students/edit/{id}")
    public String editStudent(@PathVariable Long id, Model model) {
        Student student = studentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));
        model.addAttribute("student", student);
        model.addAttribute("departments", DEPARTMENTS);
        return "student_form";
    }

    @GetMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id) {
        Student student = studentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));

        userRepository.findByUsername(student.getEnrollmentNo())
                .ifPresent(user -> userRepository.delete(user));

        studentRepo.deleteById(id);

        return "redirect:/students";
    }

    // Manage marks - merged version
    @GetMapping("/students/manage-marks/{studentId}")
    public String manageMarks(@PathVariable Long studentId, Model model) {
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + studentId));

        List<Semester> semesters = semesterRepository.findAll();

        // Create map of semesters to subjects for this student's department
        Map<Semester, List<Subject>> semesterSubjects = new LinkedHashMap<>();
        for (Semester semester : semesters) {
            List<Subject> subjects = subjectRepository.findBySemesterAndDepartment(
                    semester.getName(), student.getDepartment());
            semesterSubjects.put(semester, subjects);
        }

        model.addAttribute("student", student);
        model.addAttribute("semesterSubjects", semesterSubjects);
        model.addAttribute("semesters", semesters);

        return "manage_marks";
    }

    @GetMapping("/students/view-marks")
    public String viewMarks(@RequestParam Long studentId,
                            @RequestParam Long semesterId,
                            @RequestParam(required = false) Long subjectId,
                            Model model) {

        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + studentId));

        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid semester Id:" + semesterId));

        List<Subject> subjects = subjectRepository.findBySemesterAndDepartment(
                semester.getName(), student.getDepartment());

        List<Enrollment> enrollments;
        Subject selectedSubject = null;

        if (subjectId != null) {
            enrollments = enrollmentRepository.findByStudentIdAndSemesterIdAndSubjectId(
                            studentId, semesterId, subjectId)
                    .map(List::of)
                    .orElseGet(ArrayList::new);

            selectedSubject = subjects.stream()
                    .filter(s -> s.getId().equals(subjectId))
                    .findFirst()
                    .orElse(null);
        } else {
            enrollments = enrollmentRepository.findByStudentIdAndSemesterId(studentId, semesterId);
        }

        boolean isEditable = isCurrentSemester(semester);

        model.addAttribute("student", student);
        model.addAttribute("selectedSemester", semester);
        model.addAttribute("selectedSubject", selectedSubject);
        model.addAttribute("subjects", subjects);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("isEditable", isEditable);
        model.addAttribute("semesters", semesterRepository.findAll());

        return "manage_marks";
    }

    @PostMapping("/students/save-marks")
    public String saveMarks(@RequestParam Long studentId,
                            @RequestParam Long semesterId,
                            @RequestParam List<Long> enrollmentIds,
                            @RequestParam List<Integer> marks) {

        for (int i = 0; i < enrollmentIds.size(); i++) {
            Enrollment enrollment = enrollmentRepository.findById(enrollmentIds.get(i))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid enrollment ID"));

            int newMarks = marks.get(i);
            enrollment.setMarks(newMarks);
            enrollment.setGrade(calculateGrade(newMarks));
            enrollment.setPassed(newMarks >= 40);

            enrollmentRepository.save(enrollment);
        }

        return "redirect:/students/view-marks?studentId=" + studentId + "&semesterId=" + semesterId;
    }

    private boolean isCurrentSemester(Semester semester) {
        // TODO: Implement logic to check if semester is current
        return true;
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

    @GetMapping("/student/profile")
    public String studentProfile(Model model, Authentication authentication) {
        try {
            String enrollmentNo = authentication.getName();
            Student student = studentRepo.findByEnrollmentNo(enrollmentNo)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found"));

            model.addAttribute("student", student);
            return "student_details";
        } catch (Exception e) {
            return "redirect:/login_student?error=student_not_found";
        }
    }

    @GetMapping("/student/change-password")
    public String showChangePasswordForm() {
        return "change_password";
    }

    @PostMapping("/student/change-password")
    public String changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            Model model
    ) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            model.addAttribute("error", "Old password is incorrect");
            return "change_password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New password and confirm password do not match");
            return "change_password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        model.addAttribute("success", "Password changed successfully");
        return "change_password";
    }

    private String getDepartmentCode(String departmentName) {
        for (Map.Entry<String, String> entry : DEPARTMENTS.entrySet()) {
            if (entry.getValue().equals(departmentName)) {
                return entry.getKey();
            }
        }
        return departmentName.replaceAll("[^A-Z]", "").substring(0, 3);
    }

    private void createOrUpdateStudentUserAccount(Student student) {
        String username = student.getEnrollmentNo();

        userRepository.findByUsername(username).ifPresentOrElse(
                user -> {
                    user.setPassword(passwordEncoder.encode(student.getEnrollmentNo()));
                    userRepository.save(user);
                },
                () -> {
                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(passwordEncoder.encode(student.getEnrollmentNo()));

                    Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                            .orElseThrow(() -> new RuntimeException("Student role not found"));
                    user.setRoles(Collections.singleton(studentRole));

                    userRepository.save(user);
                }
        );
    }
}