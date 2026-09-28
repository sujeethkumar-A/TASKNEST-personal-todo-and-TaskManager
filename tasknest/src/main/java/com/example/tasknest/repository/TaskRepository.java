package com.example.tasknest.repository;

import com.example.tasknest.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByDueDate(LocalDate dueDate);

    List<Task> findByDueDateBeforeAndCompletedFalse(LocalDate date);

    List<Task> findAllByTaskList_User_Id(Long userId);

    List<Task> findByDueDateAndTaskList_User_Id(LocalDate dueDate, Long userId);

    List<Task> findByDueDateBeforeAndCompletedFalseAndTaskList_User_Id(LocalDate date, Long userId);
}