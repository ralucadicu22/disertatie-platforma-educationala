package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.ParentChildDTO;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.enums.UserRole;
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
public class ParentChildService {

    private final UserRepository userRepository;


    @Transactional
    public void linkParentChild(ParentChildDTO.LinkRequest req, User currentUser) {
        checkAdmin(currentUser);

        User parent = userRepository.findById(req.getParentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Părintele nu există"));
        User child = userRepository.findById(req.getChildId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Copilul nu există"));

        if (parent.getRole() != UserRole.PARINTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Utilizatorul " + parent.getName() + " nu are rolul de PARINTE");
        }
        if (child.getRole() != UserRole.ELEV) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Utilizatorul " + child.getName() + " nu are rolul de ELEV");
        }

        parent.getChildren().add(child);
        userRepository.save(parent);
    }

    @Transactional
    public void unlinkParentChild(Long parentId, Long childId, User currentUser) {
        checkAdmin(currentUser);

        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Părintele nu există"));
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Copilul nu există"));

        parent.getChildren().remove(child);
        userRepository.save(parent);
    }


    @Transactional(readOnly = true)
    public List<ParentChildDTO.PersonInfo> getMyChildren(User currentUser) {
        if (currentUser.getRole() != UserRole.PARINTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Doar părinții au acces la această resursă");
        }

        List<User> children = userRepository.findChildrenByParentId(currentUser.getId());
        return children.stream()
                .map(this::toPersonInfo)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParentChildDTO.PersonInfo> getMyParents(User currentUser) {
        if (currentUser.getRole() != UserRole.ELEV) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Doar elevii au acces la această resursă");
        }

        List<User> parents = userRepository.findParentsByChildId(currentUser.getId());
        return parents.stream()
                .map(this::toPersonInfo)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<ParentChildDTO.ParentWithChildren> getAllAssociations(User currentUser) {
        checkAdmin(currentUser);

        List<User> parents = userRepository.findByRole(UserRole.PARINTE);
        return parents.stream()
                .map(p -> ParentChildDTO.ParentWithChildren.builder()
                        .parentId(p.getId())
                        .parentName(p.getName())
                        .parentEmail(p.getEmail())
                        .children(p.getChildren().stream()
                                .map(this::toPersonInfo)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ParentChildDTO.ParentWithChildren getParentDetails(Long parentId, User currentUser) {
        checkAdmin(currentUser);

        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Părintele nu există"));

        if (parent.getRole() != UserRole.PARINTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilizatorul nu este părinte");
        }

        return ParentChildDTO.ParentWithChildren.builder()
                .parentId(parent.getId())
                .parentName(parent.getName())
                .parentEmail(parent.getEmail())
                .children(parent.getChildren().stream()
                        .map(this::toPersonInfo)
                        .collect(Collectors.toList()))
                .build();
    }


    private void checkAdmin(User user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Doar administratorii pot gestiona asocierile părinte-copil");
        }
    }

    private ParentChildDTO.PersonInfo toPersonInfo(User u) {
        return ParentChildDTO.PersonInfo.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .role(u.getRole())
                .build();
    }
}