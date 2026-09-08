package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    @Query("SELECT c FROM Classroom c JOIN c.teachers t WHERE t.id = :userId")
    List<Classroom> findByTeacherId(@Param("userId") Long userId);

    @Query("SELECT c FROM Classroom c JOIN c.students s WHERE s.id = :userId")
    List<Classroom> findByStudentId(@Param("userId") Long userId);

    List<Classroom> findByActiveTrue();

    @Query("SELECT c FROM Classroom c JOIN c.teachers t WHERE t.id = :userId AND c.active = true")
    List<Classroom> findActiveByTeacherId(@Param("userId") Long userId);

    @Query("SELECT c FROM Classroom c JOIN c.students s WHERE s.id = :userId AND c.active = true")
    List<Classroom> findActiveByStudentId(@Param("userId") Long userId);

    @Query("SELECT COUNT(c) > 0 FROM Classroom c JOIN c.teachers t WHERE c.id = :classroomId AND t.id = :userId")
    boolean isTeacherInClassroom(@Param("classroomId") Long classroomId, @Param("userId") Long userId);

    @Query("SELECT COUNT(c) > 0 FROM Classroom c JOIN c.students s WHERE c.id = :classroomId AND s.id = :userId")
    boolean isStudentInClassroom(@Param("classroomId") Long classroomId, @Param("userId") Long userId);
}