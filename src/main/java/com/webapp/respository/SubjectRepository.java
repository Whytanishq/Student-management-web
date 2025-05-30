package com.webapp.respository;

import com.webapp.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    boolean existsByCodeAndSemesterAndDepartment(String code, String semester, String department);

    @Query("SELECT s FROM Subject s WHERE s.semester = :semester AND " +
            "(s.department = :department OR s.department = 'ALL')")
    List<Subject> findBySemesterAndDepartment(
            @Param("semester") String semester,
            @Param("department") String department);
}
