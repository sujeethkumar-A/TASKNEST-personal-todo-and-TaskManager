package com.example.tasknest.service;

import com.example.tasknest.dto.TaskRequest;
import com.example.tasknest.entity.Task;
import com.example.tasknest.entity.TaskList;
import com.example.tasknest.repository.TaskListRepository;
import com.example.tasknest.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskListRepository taskListRepository;

    public TaskService(TaskRepository taskRepository, TaskListRepository taskListRepository) {
        this.taskRepository = taskRepository;
        this.taskListRepository = taskListRepository;
    }

    public Task createTask(TaskRequest request) {
        TaskList taskList = taskListRepository.findById(request.getTaskListId()).orElse(null);

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        task.setCompleted(false);
        task.setTaskList(taskList);

        return taskRepository.save(task);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public Task markComplete(Long id) {
        Task task = taskRepository.findById(id).orElse(null);

        if (task != null) {
            task.setCompleted(true);
            return taskRepository.save(task);
        }

        return null;
    }

    public Task markIncomplete(Long id) {
        Task task = taskRepository.findById(id).orElse(null);

        if (task != null) {
            task.setCompleted(false);
            return taskRepository.save(task);
        }

        return null;
    }

    public List<Task> getTodaysTasks() {
        return taskRepository.findByDueDate(LocalDate.now());
    }

    public List<Task> getOverdueTasks() {
        return taskRepository.findByDueDateBeforeAndCompletedFalse(LocalDate.now());
    }

    public Task moveTask(Long taskId, Long taskListId) {
        Task task = taskRepository.findById(taskId).orElse(null);
        TaskList taskList = taskListRepository.findById(taskListId).orElse(null);

        if (task != null && taskList != null) {
            task.setTaskList(taskList);
            return taskRepository.save(task);
        }

        return null;
    }
}