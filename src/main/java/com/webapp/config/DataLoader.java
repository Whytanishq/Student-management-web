package com.webapp.config;

import com.webapp.entity.Semester;
import com.webapp.entity.Subject;
import com.webapp.respository.SemesterRepository;
import com.webapp.respository.SubjectRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final SemesterRepository semesterRepository;
    private final SubjectRepository subjectRepository;

    public DataLoader(SemesterRepository semesterRepository, SubjectRepository subjectRepository) {
        this.semesterRepository = semesterRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create semesters if they don't exist
        if (semesterRepository.count() == 0) {
            List<Semester> semesters = Arrays.asList(
                    createSemester("Semester 1", "2023-24"),
                    createSemester("Semester 2", "2023-24"),
                    createSemester("Semester 3", "2024-25"),
                    createSemester("Semester 4", "2024-25"),
                    createSemester("Semester 5", "2025-26"),
                    createSemester("Semester 6", "2025-26"),
                    createSemester("Semester 7", "2026-27"),
                    createSemester("Semester 8", "2026-27")
            );
            semesterRepository.saveAll(semesters);
        }

        // Create subjects if they don't exist
        if (subjectRepository.count() == 0) {
            createCommonSubjects();
            createDepartmentSpecificSubjects();
        }
    }

    private Semester createSemester(String name, String year) {
        Semester semester = new Semester();
        semester.setName(name);
        semester.setYear(year);
        return semester;
    }

    private void createCommonSubjects() {
        // Semester 1 common subjects
        createSubject("MAT101", "Mathematics I", 4, "Semester 1", "ALL");
        createSubject("PHY101", "Physics I", 4, "Semester 1", "ALL");
        createSubject("CHE101", "Chemistry", 3, "Semester 1", "ALL");
        createSubject("BEE101", "Basic Electrical Engineering", 3, "Semester 1", "ALL");
        createSubject("EME101", "Engineering Mechanics", 3, "Semester 1", "ALL");
        createSubject("PFD101", "Programming Fundamentals", 4, "Semester 1", "ALL");
        createSubject("EDR101", "Engineering Drawing", 2, "Semester 1", "ALL");

        // Semester 2 common subjects
        createSubject("MAT102", "Mathematics II", 4, "Semester 2", "ALL");
        createSubject("PHY102", "Physics II", 4, "Semester 2", "ALL");
        createSubject("ENV101", "Environmental Science", 2, "Semester 2", "ALL");
        createSubject("BEL101", "Basic Electronics", 3, "Semester 2", "ALL");
        createSubject("WSP101", "Workshop Practice", 2, "Semester 2", "ALL");
        createSubject("DSA101", "Data Structures & Algorithms", 4, "Semester 2", "ALL");
        createSubject("COM101", "Communication Skills", 2, "Semester 2", "ALL");
    }

    private void createDepartmentSpecificSubjects() {
        // CSE Subjects
        createSubject("CSE301", "Data Structures", 4, "Semester 3", "CSE");
        createSubject("CSE302", "Discrete Math", 4, "Semester 3", "CSE");
        createSubject("CSE303", "Digital Logic Design", 4, "Semester 3", "CSE");
        createSubject("CSE304", "Computer Organization", 4, "Semester 3", "CSE");

        // ECE Subjects
        createSubject("ECE301", "Network Theory", 4, "Semester 3", "ECE");
        createSubject("ECE302", "Signals & Systems", 4, "Semester 3", "ECE");
        createSubject("ECE303", "Electronic Devices", 4, "Semester 3", "ECE");

        // Add subjects for other departments and semesters similarly...
    }

    private void createSubject(String code, String name, int credits, String semester, String department) {
        if (!subjectRepository.existsByCodeAndSemesterAndDepartment(code, semester, department)) {
            Subject subject = new Subject();
            subject.setCode(code);
            subject.setName(name);
            subject.setCredits(credits);
            subject.setSemester(semester);
            subject.setDepartment(department);
            subjectRepository.save(subject);
        }
    }
}