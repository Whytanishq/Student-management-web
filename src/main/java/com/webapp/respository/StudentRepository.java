package com.webapp.respository;

import com.webapp.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByEmail(String email);
    boolean existsByEnrollmentNo(String enrollmentNo);
    long countByEnrollmentNoStartingWith(String prefix);
    Optional<Student> findByEnrollmentNo(String enrollmentNo); // Add this line
}
