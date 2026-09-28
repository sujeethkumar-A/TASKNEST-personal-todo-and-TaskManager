package com.example.tasknest.controller;

import com.example.tasknest.dto.UserRequest;
import com.example.tasknest.dto.LoginRequest;
import com.example.tasknest.entity.User;
import com.example.tasknest.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping({"", "/register"})
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest request,
                                        HttpSession session,
                                        HttpServletRequest servletRequest) {
        try {
            User user = userService.createUser(request);
            servletRequest.changeSessionId();
            session.setAttribute("userId", user.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpSession session,
                                   HttpServletRequest servletRequest) {
        User user = userService.authenticate(request.getEmail(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email or password is incorrect.");
        }

        servletRequest.changeSessionId();
        session.setAttribute("userId", user.getId());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Long userId = getSessionUserId(session);
        User user = userId == null ? null : userService.getUserById(userId);
        return user == null
                ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
                : ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(HttpSession session) {
        Long userId = getSessionUserId(session);
        User user = userId == null ? null : userService.getUserById(userId);
        return user == null
                ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
                : ResponseEntity.ok(List.of(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, HttpSession session) {
        if (!id.equals(getSessionUserId(session))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ofNullable(userService.getUserById(id));
    }

    private Long getSessionUserId(HttpSession session) {
        Object userId = session.getAttribute("userId");
        return userId instanceof Long id ? id : null;
    }
}