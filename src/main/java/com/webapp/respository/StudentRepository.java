package com.webapp.repository;

import com.webapp.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByEmail(String email);
    boolean existsByEnrollmentNo(String enrollmentNo);
    long countByEnrollmentNoStartingWith(String prefix);
}
