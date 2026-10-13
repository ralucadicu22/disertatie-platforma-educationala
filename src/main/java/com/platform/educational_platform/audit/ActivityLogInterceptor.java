package com.platform.educational_platform.audit;

import com.platform.educational_platform.model.ActivityLog;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.repository.ActivityLogRepository;
import com.platform.educational_platform.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class ActivityLogInterceptor implements HandlerInterceptor {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    private static final String START_TIME_ATTR = "activityLogStartTime";

    private static final Map<String, ActionDescriptor> ACTIONS = Map.ofEntries(
            Map.entry("POST /api/auth/login",                    new ActionDescriptor("LOGIN", "Autentificare")),
            Map.entry("POST /api/auth/logout",                   new ActionDescriptor("LOGOUT", "Delogare")),
            Map.entry("POST /api/auth/register",                 new ActionDescriptor("REGISTER", "Înregistrare cont")),

            Map.entry("POST /api/exercises",                     new ActionDescriptor("CREATE_EXERCISE", "Exercițiu nou creat")),
            Map.entry("PUT /api/exercises",                      new ActionDescriptor("UPDATE_EXERCISE", "Exercițiu modificat")),
            Map.entry("DELETE /api/exercises",                   new ActionDescriptor("DELETE_EXERCISE", "Exercițiu șters")),
            Map.entry("POST /api/exercises/submit",              new ActionDescriptor("SUBMIT_EXERCISE", "Exercițiu rezolvat")),

            Map.entry("POST /api/quizzes",                       new ActionDescriptor("CREATE_QUIZ", "Quiz nou creat")),
            Map.entry("PUT /api/quizzes",                        new ActionDescriptor("UPDATE_QUIZ", "Quiz modificat")),
            Map.entry("DELETE /api/quizzes",                     new ActionDescriptor("DELETE_QUIZ", "Quiz șters")),

            Map.entry("POST /api/quiz-attempts",                 new ActionDescriptor("START_QUIZ", "Quiz început")),
            Map.entry("PUT /api/quiz-attempts",                  new ActionDescriptor("SUBMIT_QUIZ", "Quiz trimis")),

            Map.entry("POST /api/classrooms",                    new ActionDescriptor("CREATE_CLASSROOM", "Clasă nouă creată")),
            Map.entry("PUT /api/classrooms",                     new ActionDescriptor("UPDATE_CLASSROOM", "Clasă modificată")),
            Map.entry("DELETE /api/classrooms",                  new ActionDescriptor("DELETE_CLASSROOM", "Clasă ștearsă")),
            Map.entry("POST /api/classrooms/students",           new ActionDescriptor("ADD_STUDENT", "Student adăugat în clasă")),
            Map.entry("DELETE /api/classrooms/students",         new ActionDescriptor("REMOVE_STUDENT", "Student eliminat din clasă")),

            Map.entry("POST /api/users",                         new ActionDescriptor("CREATE_USER", "Utilizator nou creat")),
            Map.entry("PUT /api/users",                          new ActionDescriptor("UPDATE_USER", "Utilizator modificat")),
            Map.entry("DELETE /api/users",                       new ActionDescriptor("DELETE_USER", "Utilizator șters")),

            Map.entry("POST /api/quiz-assignments/assign",       new ActionDescriptor("ASSIGN_QUIZ", "Quiz asignat clasei")),
            Map.entry("DELETE /api/quiz-assignments",            new ActionDescriptor("UNASSIGN_QUIZ", "Quiz dezasignat")),

            Map.entry("POST /api/admin/impersonation/start",     new ActionDescriptor("IMPERSONATE_START", "Început View As")),
            Map.entry("POST /api/admin/impersonation/end",       new ActionDescriptor("IMPERSONATE_END", "Sfârșit View As")),
            Map.entry("POST /api/admin/bulk-import/import",      new ActionDescriptor("BULK_IMPORT", "Import bulk utilizatori")),

            Map.entry("POST /api/teacher-feedback",              new ActionDescriptor("SEND_FEEDBACK", "Feedback trimis studentului")),
            Map.entry("POST /api/exercises/versions/submit-for-review", new ActionDescriptor("SUBMIT_REVIEW", "Exercițiu trimis la review")),
            Map.entry("POST /api/exercises/versions/approve",    new ActionDescriptor("APPROVE_EXERCISE", "Exercițiu aprobat")),
            Map.entry("POST /api/exercises/versions/reject",     new ActionDescriptor("REJECT_EXERCISE", "Exercițiu respins"))
    );

    public ActivityLogInterceptor(ActivityLogRepository activityLogRepository,
                                  UserRepository userRepository) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            ActionDescriptor descriptor = matchAction(request.getMethod(), request.getRequestURI());
            if (descriptor == null) return;

            Long start = (Long) request.getAttribute(START_TIME_ATTR);
            long duration = start != null ? System.currentTimeMillis() - start : 0;

            String email = null;
            User user = null;

            Object realUserAttr = request.getAttribute("realAuthenticatedUser");
            if (realUserAttr instanceof User u) {
                user = u;
                email = u.getEmail();
            }

            if (user == null) {
                String emailFromAttr = (String) request.getAttribute("loggedInEmail");
                if (emailFromAttr != null) {
                    email = emailFromAttr;
                    user = userRepository.findByEmail(email).orElse(null);
                }
            }

            String description = descriptor.description();
            if ("IMPERSONATE_START".equals(descriptor.action()) || "IMPERSONATE_END".equals(descriptor.action())) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof User impersonated
                        && user != null && !impersonated.getId().equals(user.getId())) {
                    description = descriptor.description() + " → " + impersonated.getName()
                            + " (" + impersonated.getEmail() + ") [" + impersonated.getRole() + "]";
                }
            }

            ActivityLog logEntry = new ActivityLog();
            logEntry.setUserId(user != null ? user.getId() : null);
            logEntry.setUserEmail(email != null ? email : "anonymous");
            logEntry.setUserName(user != null ? user.getName() : null);
            logEntry.setUserRole(user != null && user.getRole() != null ? user.getRole().name() : null);
            logEntry.setAction(descriptor.action());
            logEntry.setDescription(description);
            logEntry.setHttpMethod(request.getMethod());
            logEntry.setEndpoint(request.getRequestURI());
            logEntry.setResponseStatus(response.getStatus());
            logEntry.setDurationMs(duration);
            activityLogRepository.save(logEntry);
        } catch (Exception e) {
            System.err.println("ActivityLog error: " + e.getMessage());
        }
    }

    private ActionDescriptor matchAction(String method, String uri) {
        String stripped = uri.replaceAll("/\\d+(?=/|$)", "");
        ActionDescriptor result = ACTIONS.get(method + " " + stripped);

        return result;
    }

    private record ActionDescriptor(String action, String description) {}

}
