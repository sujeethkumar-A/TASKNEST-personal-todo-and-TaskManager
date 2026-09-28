package com.example.tasknest.controller;

import com.example.tasknest.dto.TaskListRequest;
import com.example.tasknest.entity.TaskList;
import com.example.tasknest.service.TaskListService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task-lists")
public class TaskListController {

    private final TaskListService taskListService;

    public TaskListController(TaskListService taskListService) {
        this.taskListService = taskListService;
    }

    @PostMapping
    public ResponseEntity<?> createTaskList(@Valid @RequestBody TaskListRequest request, HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TaskList taskList = taskListService.createTaskList(request, userId);
        return taskList == null ? ResponseEntity.notFound().build() : ResponseEntity.status(HttpStatus.CREATED).body(taskList);
    }

    @GetMapping
    public ResponseEntity<?> getAllTaskLists(HttpSession session) {
        Long userId = getSessionUserId(session);
        return userId == null
                ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
                : ResponseEntity.ok(taskListService.getAllTaskLists(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskListById(@PathVariable Long id, HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ofNullable(taskListService.getTaskListById(id, userId));
    }

    private Long getSessionUserId(HttpSession session) {
        Object userId = session.getAttribute("userId");
        return userId instanceof Long id ? id : null;
    }
}