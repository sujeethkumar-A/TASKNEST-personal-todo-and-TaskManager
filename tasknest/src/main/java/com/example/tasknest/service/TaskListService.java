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

    public TaskList createTaskList(TaskListRequest request, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        TaskList taskList = new TaskList();
        taskList.setName(request.getName());
        taskList.setUser(user);

        return taskListRepository.save(taskList);
    }

    public List<TaskList> getAllTaskLists(Long userId) {
        return taskListRepository.findAllByUser_Id(userId);
    }

    public TaskList getTaskListById(Long id, Long userId) {
        return taskListRepository.findById(id)
                .filter(taskList -> taskList.getUser() != null
                        && userId.equals(taskList.getUser().getId()))
                .orElse(null);
    }
}