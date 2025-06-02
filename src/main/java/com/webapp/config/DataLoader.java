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
        // Semester 3 Subjects
        createSubject("CSE301", "Data Structures", 4, "Semester 3", "CSE");
        createSubject("CSE302", "Discrete Math", 4, "Semester 3", "CSE");
        createSubject("CSE303", "Digital Logic Design", 4, "Semester 3", "CSE");
        createSubject("CSE304", "Computer Organization", 4, "Semester 3", "CSE");

        createSubject("ECE301", "Network Theory", 4, "Semester 3", "ECE");
        createSubject("ECE302", "Signals & Systems", 4, "Semester 3", "ECE");
        createSubject("ECE303", "Electronic Devices", 4, "Semester 3", "ECE");

        createSubject("EEE301", "Electrical Machines I", 4, "Semester 3", "EEE");
        createSubject("EEE302", "Network Theory", 4, "Semester 3", "EEE");

        createSubject("MEC301", "Engineering Thermodynamics", 4, "Semester 3", "MEC");
        createSubject("MEC302", "Fluid Mechanics", 4, "Semester 3", "MEC");

        createSubject("CIV301", "Surveying", 4, "Semester 3", "CIV");
        createSubject("CIV302", "Building Materials", 4, "Semester 3", "CIV");

        createSubject("INF301", "Data Structures", 4, "Semester 3", "INF");
        createSubject("INF302", "Discrete Math", 4, "Semester 3", "INF");

        createSubject("AID301", "Linear Algebra", 4, "Semester 3", "AID");
        createSubject("AID302", "Probability & Statistics", 4, "Semester 3", "AID");

        createSubject("DSC301", "Linear Algebra", 4, "Semester 3", "DSC");
        createSubject("DSC302", "Probability & Statistics", 4, "Semester 3", "DSC");

        // Semester 4 Subjects
        createSubject("CSE401", "Theory of Computation", 4, "Semester 4", "CSE");
        createSubject("CSE402", "Operating Systems", 4, "Semester 4", "CSE");
        createSubject("CSE403", "Microprocessors", 4, "Semester 4", "CSE");

        createSubject("ECE401", "Analog Circuits", 4, "Semester 4", "ECE");
        createSubject("ECE402", "Electromagnetic Fields", 4, "Semester 4", "ECE");
        createSubject("ECE403", "Digital Communication", 4, "Semester 4", "ECE");

        createSubject("EEE401", "Electrical Machines II", 4, "Semester 4", "EEE");
        createSubject("EEE402", "Control Systems", 4, "Semester 4", "EEE");

        createSubject("MEC401", "Manufacturing Technology", 4, "Semester 4", "MEC");
        createSubject("MEC402", "Mechanics of Solids", 4, "Semester 4", "MEC");

        createSubject("CIV401", "Structural Analysis", 4, "Semester 4", "CIV");
        createSubject("CIV402", "Soil Mechanics", 4, "Semester 4", "CIV");

        createSubject("INF401", "Theory of Computation", 4, "Semester 4", "INF");
        createSubject("INF402", "Operating Systems", 4, "Semester 4", "INF");

        createSubject("AID401", "Machine Learning Fundamentals", 4, "Semester 4", "AID");
        createSubject("DSC401", "Machine Learning Fundamentals", 4, "Semester 4", "DSC");

        // Semester 5 Subjects
        createSubject("CSE501", "Databases", 4, "Semester 5", "CSE");
        createSubject("CSE502", "Computer Networks", 4, "Semester 5", "CSE");
        createSubject("CSE503", "Software Engineering", 4, "Semester 5", "CSE");

        createSubject("ECE501", "Communication Systems", 4, "Semester 5", "ECE");
        createSubject("ECE502", "VLSI Design", 4, "Semester 5", "ECE");

        createSubject("EEE501", "Power Systems", 4, "Semester 5", "EEE");
        createSubject("EEE502", "Electrical Measurements", 4, "Semester 5", "EEE");

        createSubject("MEC501", "Machine Design", 4, "Semester 5", "MEC");
        createSubject("MEC502", "Heat Transfer", 4, "Semester 5", "MEC");

        createSubject("CIV501", "Concrete Technology", 4, "Semester 5", "CIV");
        createSubject("CIV502", "Transportation Engineering", 4, "Semester 5", "CIV");

        createSubject("INF501", "Databases", 4, "Semester 5", "INF");
        createSubject("INF502", "Computer Networks", 4, "Semester 5", "INF");

        createSubject("AID501", "Data Mining", 4, "Semester 5", "AID");
        createSubject("AID502", "Deep Learning", 4, "Semester 5", "AID");

        createSubject("DSC501", "Data Mining", 4, "Semester 5", "DSC");
        createSubject("DSC502", "Deep Learning", 4, "Semester 5", "DSC");

        // Semester 6 Subjects
        createSubject("CSE601", "Artificial Intelligence", 4, "Semester 6", "CSE");
        createSubject("CSE602", "Compiler Design", 4, "Semester 6", "CSE");
        createSubject("CSE603", "Information Security", 4, "Semester 6", "CSE");

        createSubject("ECE601", "Digital Signal Processing", 4, "Semester 6", "ECE");
        createSubject("ECE602", "Embedded Systems", 4, "Semester 6", "ECE");

        createSubject("EEE601", "Electrical Drives", 4, "Semester 6", "EEE");
        createSubject("EEE602", "Power Electronics", 4, "Semester 6", "EEE");

        createSubject("MEC601", "Dynamics of Machines", 4, "Semester 6", "MEC");
        createSubject("MEC602", "HVAC", 4, "Semester 6", "MEC");

        createSubject("CIV601", "Environmental Engineering", 4, "Semester 6", "CIV");
        createSubject("CIV602", "Structural Design", 4, "Semester 6", "CIV");

        createSubject("INF601", "Artificial Intelligence", 4, "Semester 6", "INF");
        createSubject("INF602", "Compiler Design", 4, "Semester 6", "INF");

        createSubject("AID601", "Natural Language Processing", 4, "Semester 6", "AID");
        createSubject("AID602", "Computer Vision", 4, "Semester 6", "AID");

        createSubject("DSC601", "Natural Language Processing", 4, "Semester 6", "DSC");
        createSubject("DSC602", "Computer Vision", 4, "Semester 6", "DSC");

        // Semester 7 Subjects
        createSubject("CSE701", "Cloud Computing", 4, "Semester 7", "CSE");
        createSubject("CSE702", "Big Data Analytics", 4, "Semester 7", "CSE");
        createSubject("CSE703", "Elective Subjects", 4, "Semester 7", "CSE");

        createSubject("ECE701", "Microwave Engineering", 4, "Semester 7", "ECE");
        createSubject("ECE702", "Robotics", 4, "Semester 7", "ECE");
        createSubject("ECE703", "Elective Subjects", 4, "Semester 7", "ECE");

        createSubject("EEE701", "High Voltage Engineering", 4, "Semester 7", "EEE");
        createSubject("EEE702", "Smart Grids", 4, "Semester 7", "EEE");

        createSubject("MEC701", "CAD/CAM", 4, "Semester 7", "MEC");
        createSubject("MEC702", "Renewable Energy", 4, "Semester 7", "MEC");

        createSubject("CIV701", "Advanced Concrete Technology", 4, "Semester 7", "CIV");
        createSubject("CIV702", "Elective Subjects", 4, "Semester 7", "CIV");

        createSubject("INF701", "Cloud Computing", 4, "Semester 7", "INF");
        createSubject("INF702", "Big Data Analytics", 4, "Semester 7", "INF");

        createSubject("AID701", "Reinforcement Learning", 4, "Semester 7", "AID");
        createSubject("AID702", "Advanced AI Electives", 4, "Semester 7", "AID");

        createSubject("DSC701", "Reinforcement Learning", 4, "Semester 7", "DSC");
        createSubject("DSC702", "Advanced AI Electives", 4, "Semester 7", "DSC");

        // Semester 8 Subjects
        createSubject("CSE801", "Project", 6, "Semester 8", "CSE");
        createSubject("CSE802", "Seminar", 2, "Semester 8", "CSE");
        createSubject("CSE803", "Electives", 4, "Semester 8", "CSE");

        createSubject("ECE801", "Project", 6, "Semester 8", "ECE");
        createSubject("ECE802", "Seminar", 2, "Semester 8", "ECE");
        createSubject("ECE803", "Electives", 4, "Semester 8", "ECE");

        createSubject("EEE801", "Project", 6, "Semester 8", "EEE");
        createSubject("EEE802", "Seminar", 2, "Semester 8", "EEE");
        createSubject("EEE803", "Electives", 4, "Semester 8", "EEE");

        createSubject("MEC801", "Project", 6, "Semester 8", "MEC");
        createSubject("MEC802", "Seminar", 2, "Semester 8", "MEC");
        createSubject("MEC803", "Electives", 4, "Semester 8", "MEC");

        createSubject("CIV801", "Project", 6, "Semester 8", "CIV");
        createSubject("CIV802", "Seminar", 2, "Semester 8", "CIV");
        createSubject("CIV803", "Electives", 4, "Semester 8", "CIV");

        createSubject("INF801", "Project", 6, "Semester 8", "INF");
        createSubject("INF802", "Seminar", 2, "Semester 8", "INF");
        createSubject("INF803", "Electives", 4, "Semester 8", "INF");

        createSubject("AID801", "Project", 6, "Semester 8", "AID");
        createSubject("AID802", "Seminar", 2, "Semester 8", "AID");
        createSubject("AID803", "Electives", 4, "Semester 8", "AID");

        createSubject("DSC801", "Project", 6, "Semester 8", "DSC");
        createSubject("DSC802", "Seminar", 2, "Semester 8", "DSC");
        createSubject("DSC803", "Electives", 4, "Semester 8", "DSC");
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
