package com.webapp.respository;

import com.webapp.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SemesterRepository extends JpaRepository<Semester, Long> {
    Optional<Semester> findByName(String semesterName);
}
