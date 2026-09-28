package com.example.tasknest.controller;

import com.example.tasknest.dto.TaskRequest;
import com.example.tasknest.entity.Task;
import com.example.tasknest.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public Task createTask(@RequestBody TaskRequest request) {
        return taskService.createTask(request);
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/today")
    public List<Task> getTodaysTasks() {
        return taskService.getTodaysTasks();
    }

    @GetMapping("/overdue")
    public List<Task> getOverdueTasks() {
        return taskService.getOverdueTasks();
    }

    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PutMapping("/{id}/complete")
    public Task markComplete(@PathVariable Long id) {
        return taskService.markComplete(id);
    }

    @PutMapping("/{id}/incomplete")
    public Task markIncomplete(@PathVariable Long id) {
        return taskService.markIncomplete(id);
    }
    @PutMapping("/{taskId}/move/{taskListId}")
public Task moveTask(@PathVariable Long taskId,
                     @PathVariable Long taskListId) {
    return taskService.moveTask(taskId, taskListId);
}
}