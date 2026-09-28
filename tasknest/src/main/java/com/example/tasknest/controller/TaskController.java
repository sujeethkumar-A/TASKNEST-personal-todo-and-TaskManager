package com.example.tasknest.controller;

import com.example.tasknest.dto.TaskRequest;
import com.example.tasknest.entity.Task;
import com.example.tasknest.service.TaskService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskRequest request, HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Task task = taskService.createTask(request, userId);
        return task == null ? ResponseEntity.notFound().build() : ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @GetMapping
    public ResponseEntity<?> getAllTasks(HttpSession session) {
        Long userId = getSessionUserId(session);
        return userId == null
                ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
                : ResponseEntity.ok(taskService.getAllTasks(userId));
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodaysTasks(HttpSession session) {
        Long userId = getSessionUserId(session);
        return userId == null
                ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
                : ResponseEntity.ok(taskService.getTodaysTasks(userId));
    }

    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueTasks(HttpSession session) {
        Long userId = getSessionUserId(session);
        return userId == null
                ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
                : ResponseEntity.ok(taskService.getOverdueTasks(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id, HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ofNullable(taskService.getTaskById(id, userId));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<?> markComplete(@PathVariable Long id, HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ofNullable(taskService.markComplete(id, userId));
    }

    @PutMapping("/{id}/incomplete")
    public ResponseEntity<?> markIncomplete(@PathVariable Long id, HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ofNullable(taskService.markIncomplete(id, userId));
    }

    @PutMapping("/{taskId}/move/{taskListId}")
    public ResponseEntity<?> moveTask(@PathVariable Long taskId,
                                      @PathVariable Long taskListId,
                                      HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ofNullable(taskService.moveTask(taskId, taskListId, userId));
    }

    private Long getSessionUserId(HttpSession session) {
        Object userId = session.getAttribute("userId");
        return userId instanceof Long id ? id : null;
    }
}