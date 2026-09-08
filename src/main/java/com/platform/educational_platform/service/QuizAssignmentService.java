package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.QuizAssignmentDTO;
import com.platform.educational_platform.model.Classroom;
import com.platform.educational_platform.model.Quiz;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.enums.UserRole;
import com.platform.educational_platform.repository.ClassroomRepository;
import com.platform.educational_platform.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizAssignmentService {

    private final ClassroomRepository classroomRepository;
    private final QuizRepository quizRepository;


    @Transactional
    public void assignQuizzesToClassrooms(QuizAssignmentDTO.AssignRequest req, User currentUser) {
        List<Quiz> quizzes = quizRepository.findAllById(req.getQuizIds());
        if (quizzes.size() != req.getQuizIds().size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Un quiz din listă nu există");
        }

        List<Classroom> classrooms = classroomRepository.findAllById(req.getClassroomIds());
        if (classrooms.size() != req.getClassroomIds().size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "O clasă din listă nu există");
        }


        for (Classroom c : classrooms) {
            checkTeacherOrAdmin(c, currentUser);
        }


        for (Classroom c : classrooms) {
            c.getAssignedQuizzes().addAll(quizzes);
        }

        classroomRepository.saveAll(classrooms);
    }

    @Transactional
    public void unassignQuiz(Long classroomId, Long quizId, User currentUser) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clasa nu există"));
        checkTeacherOrAdmin(classroom, currentUser);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quizul nu există"));

        classroom.getAssignedQuizzes().remove(quiz);
        classroomRepository.save(classroom);
    }


    @Transactional(readOnly = true)
    public List<QuizAssignmentDTO.ClassroomQuizzes> getMyClassroomsWithQuizzes(User currentUser) {
        List<Classroom> classrooms;
        if (currentUser.getRole() == UserRole.PROFESOR) {
            classrooms = classroomRepository.findActiveByTeacherId(currentUser.getId());
        } else {
            classrooms = classroomRepository.findByActiveTrue();
        }

        return classrooms.stream()
                .map(c -> QuizAssignmentDTO.ClassroomQuizzes.builder()
                        .classroomId(c.getId())
                        .classroomName(c.getName())
                        .quizzes(c.getAssignedQuizzes().stream()
                                .map(q -> QuizAssignmentDTO.QuizInfo.builder()
                                        .id(q.getId())
                                        .title(q.getTitle())
                                        .description(q.getDescription())
                                        .exerciseCount(q.getExercises() != null ? q.getExercises().size() : 0)
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuizAssignmentDTO.AssignedQuizInfo> getMyAssignedQuizzes(User currentUser) {
        if (currentUser.getRole() != UserRole.ELEV) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Doar elevii pot vedea quizurile asignate");
        }

        List<Classroom> classrooms = classroomRepository.findActiveByStudentId(currentUser.getId());

        return classrooms.stream()
                .flatMap(c -> c.getAssignedQuizzes().stream()
                        .map(q -> QuizAssignmentDTO.AssignedQuizInfo.builder()
                                .quizId(q.getId())
                                .quizTitle(q.getTitle())
                                .quizDescription(q.getDescription())
                                .classroomId(c.getId())
                                .classroomName(c.getName())
                                .exerciseCount(q.getExercises() != null ? q.getExercises().size() : 0)
                                .build()))
                .collect(Collectors.toList());
    }


    private void checkTeacherOrAdmin(Classroom classroom, User user) {
        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        boolean isTeacher = classroom.getTeachers().stream()
                .anyMatch(t -> t.getId().equals(user.getId()));
        if (!isAdmin && !isTeacher) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Nu ai acces la clasa " + classroom.getName());
        }
    }
}