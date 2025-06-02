package com.webapp.controller;

import com.webapp.entity.*;
import com.webapp.respository.*;
import com.webapp.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class StudentController {

    private final StudentRepository studentRepo;


    // Add this at the top with other autowired repositories
    @Autowired
    private EnrollmentService enrollmentService;

    // Add this new endpoint
    @GetMapping("/students/enroll-all")
    public String enrollAllStudents() {
        enrollmentService.enrollAllStudents();
        return "redirect:/students?syncSuccess=true";
    }

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
    public String studentManagement(Model model, @RequestParam(required = false) Boolean syncSuccess) {
        List<Student> students = studentRepo.findAll();
        model.addAttribute("students", students);
        if (Boolean.TRUE.equals(syncSuccess)) {
            model.addAttribute("successMessage", "Student accounts synchronized successfully");
        }
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

        // Enroll the student in their semesters
        enrollmentService.enrollStudent(student);

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
                .ifPresent(userRepository::delete);

        studentRepo.deleteById(id);

        return "redirect:/students";
    }

    // Manage marks - fixed to avoid 500 errors and pass all required attributes
    @GetMapping("/students/manage-marks/{studentId}")
    public String manageMarks(@PathVariable Long studentId, Model model) {
        Optional<Student> studentOpt = studentRepo.findById(studentId);
        if (studentOpt.isEmpty()) {
            model.addAttribute("error", "Student with ID " + studentId + " not found.");
            return "error_page";
        }

        Student student = studentOpt.get();
        List<Semester> allSemesters = semesterRepository.findAll();
        int currentSemesterNumber = calculateCurrentSemesterNumber(student);

        List<Semester> availableSemesters = allSemesters.stream()
                .filter(s -> {
                    int semesterNumber = Integer.parseInt(s.getName().replaceAll("[^0-9]", ""));
                    return semesterNumber <= currentSemesterNumber;
                })
                .collect(Collectors.toList());

        // Initialize isCurrentSemester to false by default
        boolean isCurrentSemester = false;
        Semester selectedSemester = null;
        List<Enrollment> enrollments = new ArrayList<>();

        if (!availableSemesters.isEmpty()) {
            selectedSemester = availableSemesters.get(0);
            int selectedSemesterNumber = Integer.parseInt(selectedSemester.getName().replaceAll("[^0-9]", ""));
            isCurrentSemester = (selectedSemesterNumber == currentSemesterNumber);

            enrollments = enrollmentRepository.findByStudentIdAndSemesterId(studentId, selectedSemester.getId());
            if (enrollments.isEmpty()) {
                model.addAttribute("warning", "No enrollment records found for this student");
            }
        }

        model.addAttribute("student", student);
        model.addAttribute("semesters", availableSemesters);
        model.addAttribute("currentSemesterNumber", currentSemesterNumber);
        model.addAttribute("isCurrentSemester", isCurrentSemester);
        model.addAttribute("selectedSemester", selectedSemester);
        model.addAttribute("enrollments", enrollments);

        return "manage_marks";
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

    @GetMapping("/students/view-marks")
    public String viewMarks(@RequestParam Long studentId,
                            @RequestParam Long semesterId,
                            @RequestParam(required = false) Long subjectId,
                            Model model) {

        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + studentId));

        Semester selectedSemester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid semester Id:" + semesterId));

        int currentSemesterNumber = calculateCurrentSemesterNumber(student);
        int selectedSemesterNumber = Integer.parseInt(selectedSemester.getName().replaceAll("[^0-9]", ""));
        boolean isCurrentSemester = (selectedSemesterNumber == currentSemesterNumber);

        List<Semester> allSemesters = semesterRepository.findAll();
        List<Semester> availableSemesters = allSemesters.stream()
                .filter(s -> {
                    int semNum = Integer.parseInt(s.getName().replaceAll("[^0-9]", ""));
                    return semNum <= currentSemesterNumber;
                })
                .sorted(Comparator.comparing(s -> Integer.parseInt(s.getName().replaceAll("[^0-9]", ""))))
                .collect(Collectors.toList());

        List<Subject> subjects = subjectRepository.findBySemesterAndDepartment(
                selectedSemester.getName(), student.getDepartment());

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

        model.addAttribute("student", student);
        model.addAttribute("selectedSemester", selectedSemester);
        model.addAttribute("semesters", availableSemesters);
        model.addAttribute("subjects", subjects);
        model.addAttribute("selectedSubject", selectedSubject);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("isEditable", isCurrentSemester);
        model.addAttribute("currentSemesterNumber", currentSemesterNumber);
        model.addAttribute("isCurrentSemester", isCurrentSemester);

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

    @GetMapping("/sync-student-accounts")
    public String syncStudentAccounts() {
        List<Student> students = studentRepo.findAll();
        students.forEach(this::createOrUpdateStudentUserAccount);
        return "redirect:/students?syncSuccess=true";
    }

    @GetMapping("/validate-student/{enrollmentNo}")
    @ResponseBody
    public ResponseEntity<String> validateStudent(@PathVariable String enrollmentNo) {
        Optional<Student> student = studentRepo.findByEnrollmentNo(enrollmentNo);
        Optional<User> user = userRepository.findByUsername(enrollmentNo);

        if (student.isEmpty()) {
            return ResponseEntity.badRequest().body("Student record not found");
        }

        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("User account not found");
        }

        return ResponseEntity.ok("Student and user account exist");
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
        for (Map.Entry<String, String> entry : DEPARTMENTS.entrySet()) {
            if (entry.getValue().equals(departmentName)) {
                return entry.getKey();
            }
        }
        // fallback: Extract uppercase letters and take first 3 letters or less
        String uppercase = departmentName.replaceAll("[^A-Z]", "");
        if (uppercase.length() >= 3) {
            return uppercase.substring(0, 3);
        } else if (!uppercase.isEmpty()) {
            return uppercase;
        } else {
            // If no uppercase letters, fallback to first 3 letters uppercase
            return departmentName.length() >=3 ?
                    departmentName.substring(0, 3).toUpperCase() :
                    departmentName.toUpperCase();
        }
    }

    private void createOrUpdateStudentUserAccount(Student student) {
        String username = student.getEnrollmentNo();

        userRepository.findByUsername(username).ifPresentOrElse(
                user -> {
                    // Reset password to enrollmentNo encoded - consider if you want this on update
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
