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

    public Task createTask(TaskRequest request, Long userId) {
        TaskList taskList = taskListRepository.findById(request.getTaskListId())
                .filter(list -> list.getUser() != null && userId.equals(list.getUser().getId()))
                .orElse(null);
        if (taskList == null) {
            return null;
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        task.setCompleted(false);
        task.setTaskList(taskList);

        return taskRepository.save(task);
    }

    public List<Task> getAllTasks(Long userId) {
        return taskRepository.findAllByTaskList_User_Id(userId);
    }

    public Task getTaskById(Long id, Long userId) {
        return taskRepository.findById(id).filter(task -> belongsTo(task, userId)).orElse(null);
    }

    public Task markComplete(Long id, Long userId) {
        Task task = getTaskById(id, userId);

        if (task != null) {
            task.setCompleted(true);
            return taskRepository.save(task);
        }

        return null;
    }

    public Task markIncomplete(Long id, Long userId) {
        Task task = getTaskById(id, userId);

        if (task != null) {
            task.setCompleted(false);
            return taskRepository.save(task);
        }

        return null;
    }

    public List<Task> getTodaysTasks(Long userId) {
        return taskRepository.findByDueDateAndTaskList_User_Id(LocalDate.now(), userId);
    }

    public List<Task> getOverdueTasks(Long userId) {
        return taskRepository.findByDueDateBeforeAndCompletedFalseAndTaskList_User_Id(LocalDate.now(), userId);
    }

    public Task moveTask(Long taskId, Long taskListId, Long userId) {
        Task task = getTaskById(taskId, userId);
        TaskList taskList = taskListRepository.findById(taskListId)
                .filter(list -> list.getUser() != null && userId.equals(list.getUser().getId()))
                .orElse(null);

        if (task != null && taskList != null) {
            task.setTaskList(taskList);
            return taskRepository.save(task);
        }

        return null;
    }

    private boolean belongsTo(Task task, Long userId) {
        return task.getTaskList() != null
                && task.getTaskList().getUser() != null
                && userId.equals(task.getTaskList().getUser().getId());
    }
}