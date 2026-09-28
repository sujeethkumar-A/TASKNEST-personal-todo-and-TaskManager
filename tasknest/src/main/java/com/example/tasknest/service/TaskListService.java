package com.example.tasknest.service;

import com.example.tasknest.dto.TaskListRequest;
import com.example.tasknest.entity.TaskList;
import com.example.tasknest.entity.User;
import com.example.tasknest.repository.TaskListRepository;
import com.example.tasknest.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskListService {

    private final TaskListRepository taskListRepository;
    private final UserRepository userRepository;

    public TaskListService(TaskListRepository taskListRepository,
                           UserRepository userRepository) {
        this.taskListRepository = taskListRepository;
        this.userRepository = userRepository;
    }

    public TaskList createTaskList(TaskListRequest request) {
        User user = userRepository.findById(request.getUserId()).orElse(null);

        TaskList taskList = new TaskList();
        taskList.setName(request.getName());
        taskList.setUser(user);

        return taskListRepository.save(taskList);
    }

    public List<TaskList> getAllTaskLists() {
        return taskListRepository.findAll();
    }

    public TaskList getTaskListById(Long id) {
        return taskListRepository.findById(id).orElse(null);
    }
}