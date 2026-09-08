package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.ClassroomDTO;
import com.platform.educational_platform.model.Classroom;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.enums.UserRole;
import com.platform.educational_platform.repository.ClassroomRepository;
import com.platform.educational_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;



    @Transactional
    public ClassroomDTO.Detail createClassroom(ClassroomDTO.CreateRequest req, User createdBy) {
        Classroom classroom = Classroom.builder()
                .name(req.getName())
                .description(req.getDescription())
                .schoolYear(req.getSchoolYear())
                .gradeLevel(req.getGradeLevel())
                .build();

        if (createdBy.getRole() == UserRole.PROFESOR || createdBy.getRole() == UserRole.ADMIN) {
            classroom.getTeachers().add(createdBy);
        }

        return toDetail(classroomRepository.save(classroom));
    }

    @Transactional
    public ClassroomDTO.Detail updateClassroom(Long id, ClassroomDTO.UpdateRequest req, User currentUser) {
        Classroom classroom = findOrThrow(id);
        checkTeacherOrAdmin(classroom, currentUser);

        if (req.getName() != null) classroom.setName(req.getName());
        if (req.getDescription() != null) classroom.setDescription(req.getDescription());
        if (req.getSchoolYear() != null) classroom.setSchoolYear(req.getSchoolYear());
        if (req.getGradeLevel() != null) classroom.setGradeLevel(req.getGradeLevel());
        if (req.getActive() != null) classroom.setActive(req.getActive());

        return toDetail(classroomRepository.save(classroom));
    }

    @Transactional
    public void deleteClassroom(Long id, User currentUser) {
        Classroom classroom = findOrThrow(id);
        checkAdmin(currentUser);
        classroomRepository.delete(classroom);
    }

    @Transactional(readOnly = true)
    public ClassroomDTO.Detail getClassroom(Long id) {
        return toDetail(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ClassroomDTO.Summary> getAllClassrooms() {
        return classroomRepository.findAll().stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClassroomDTO.Summary> getMyClassrooms(User currentUser) {
        List<Classroom> classrooms;
        if (currentUser.getRole() == UserRole.PROFESOR) {
            classrooms = classroomRepository.findActiveByTeacherId(currentUser.getId());
        } else if (currentUser.getRole() == UserRole.ELEV) {
            classrooms = classroomRepository.findActiveByStudentId(currentUser.getId());
        } else {
            classrooms = classroomRepository.findByActiveTrue();
        }
        return classrooms.stream().map(this::toSummary).collect(Collectors.toList());
    }


    @Transactional
    public ClassroomDTO.Detail addTeacher(Long classroomId, Long userId, User currentUser) {
        Classroom classroom = findOrThrow(classroomId);
        checkAdmin(currentUser);

        User teacher = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User negăsit"));

        if (teacher.getRole() != UserRole.PROFESOR && teacher.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Utilizatorul nu are rolul de PROFESOR");
        }

        classroom.getTeachers().add(teacher);
        return toDetail(classroomRepository.save(classroom));
    }

    @Transactional
    public ClassroomDTO.Detail removeTeacher(Long classroomId, Long userId, User currentUser) {
        Classroom classroom = findOrThrow(classroomId);
        checkAdmin(currentUser);

        User teacher = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User negăsit"));

        classroom.getTeachers().remove(teacher);
        return toDetail(classroomRepository.save(classroom));
    }

    @Transactional
    public ClassroomDTO.Detail addStudent(Long classroomId, Long userId, User currentUser) {
        Classroom classroom = findOrThrow(classroomId);
        checkTeacherOrAdmin(classroom, currentUser);

        User student = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User negăsit"));

        if (student.getRole() != UserRole.ELEV) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Utilizatorul nu are rolul de ELEV");
        }

        classroom.getStudents().add(student);
        return toDetail(classroomRepository.save(classroom));
    }

    @Transactional
    public ClassroomDTO.Detail removeStudent(Long classroomId, Long userId, User currentUser) {
        Classroom classroom = findOrThrow(classroomId);
        checkTeacherOrAdmin(classroom, currentUser);

        User student = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User negăsit"));

        classroom.getStudents().remove(student);
        return toDetail(classroomRepository.save(classroom));
    }


    private Classroom findOrThrow(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Clasa cu id=" + id + " nu există"));
    }

    private void checkTeacherOrAdmin(Classroom classroom, User user) {
        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        boolean isTeacher = classroom.getTeachers().stream()
                .anyMatch(t -> t.getId().equals(user.getId()));
        if (!isAdmin && !isTeacher) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Nu ai acces la această clasă");
        }
    }

    private void checkAdmin(User user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Doar administratorii pot efectua această operație");
        }
    }

    public ClassroomDTO.Summary toSummary(Classroom c) {
        return ClassroomDTO.Summary.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .schoolYear(c.getSchoolYear())
                .gradeLevel(c.getGradeLevel())
                .active(c.getActive())
                .createdAt(c.getCreatedAt())
                .teacherCount(c.getTeachers().size())
                .studentCount(c.getStudents().size())
                .quizCount(c.getAssignedQuizzes().size())
                .build();
    }

    public ClassroomDTO.Detail toDetail(Classroom c) {
        return ClassroomDTO.Detail.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .schoolYear(c.getSchoolYear())
                .gradeLevel(c.getGradeLevel())
                .active(c.getActive())
                .createdAt(c.getCreatedAt())
                .teachers(c.getTeachers().stream()
                .map(u -> ClassroomDTO.MemberInfo.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .role(u.getRole())
                        .build())
                .collect(Collectors.toList()))
                .students(c.getStudents().stream()
                        .map(u -> ClassroomDTO.MemberInfo.builder()
                                .id(u.getId())
                                .name(u.getName())
                                .email(u.getEmail())
                                .role(u.getRole())
                                .build())
                        .collect(Collectors.toList()))
                .assignedQuizzes(c.getAssignedQuizzes().stream()
                        .map(q -> ClassroomDTO.QuizInfo.builder()
                                .id(q.getId())
                                .title(q.getTitle())
                                .description(q.getDescription())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}