package com.example.tasknest.repository;

import com.example.tasknest.entity.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskListRepository extends JpaRepository<TaskList, Long> {
    
}