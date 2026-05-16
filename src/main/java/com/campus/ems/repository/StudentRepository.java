package com.campus.ems.repository;

import com.campus.ems.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
    boolean existsByEmail(String email);

    List<Student> findByDepartment(String department);

    @Query("SELECT s FROM Student s WHERE " +
           "(:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "   OR LOWER(s.email) LIKE LOWER(CONCAT('%',:keyword,'%'))) " +
           "AND (:department IS NULL OR s.department = :department) " +
           "AND (:year IS NULL OR s.year = :year) " +
           "ORDER BY s.name ASC")
    List<Student> searchStudents(@Param("keyword") String keyword,
                                  @Param("department") String department,
                                  @Param("year") String year);
}
