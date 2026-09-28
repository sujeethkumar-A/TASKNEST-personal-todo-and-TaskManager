package com.example.tasknest.controller;

import com.example.tasknest.dto.TaskListRequest;
import com.example.tasknest.entity.TaskList;
import com.example.tasknest.service.TaskListService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task-lists")
public class TaskListController {

    private final TaskListService taskListService;

    public TaskListController(TaskListService taskListService) {
        this.taskListService = taskListService;
    }

    @PostMapping
    public TaskList createTaskList(@RequestBody TaskListRequest request) {
        return taskListService.createTaskList(request);
    }

    @GetMapping
    public List<TaskList> getAllTaskLists() {
        return taskListService.getAllTaskLists();
    }

    @GetMapping("/{id}")
    public TaskList getTaskListById(@PathVariable Long id) {
        return taskListService.getTaskListById(id);
    }
}