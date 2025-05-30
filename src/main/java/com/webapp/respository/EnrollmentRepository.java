package com.webapp.respository;

import com.webapp.entity.Enrollment;
import com.webapp.entity.Semester;
import com.webapp.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentAndSemester(Student student, Semester semester);
    List<Enrollment> findByStudent(Student student);
    List<Enrollment> findByStudentOrderBySemesterIdDesc(Student student);
    List<Enrollment> findByStudentAndSemesterOrderBySubjectId(Student student, Semester semester);
}
