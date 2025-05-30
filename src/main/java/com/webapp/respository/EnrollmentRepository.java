package com.webapp.respository;  // fix typo from 'respository'

import com.webapp.entity.Enrollment;
import com.webapp.entity.Semester;
import com.webapp.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudent(Student student);
    List<Enrollment> findByStudentOrderBySemesterIdDesc(Student student);
    List<Enrollment> findByStudentAndSemesterOrderBySubjectId(Student student, Semester semester);

    // Add these two methods (you used in controller but not declared)
    List<Enrollment> findByStudentIdAndSemesterId(Long studentId, Long semesterId);

    Optional<Enrollment> findByStudentIdAndSemesterIdAndSubjectId(Long studentId, Long semesterId, Long subjectId);
}
