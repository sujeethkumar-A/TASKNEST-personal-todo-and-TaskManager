package com.example.tasknest.dto;
import com.example.tasknest.enums.Priority;
import java.time.LocalDate;

public class TaskRequest {
    private String title;
    private long taskListId;
    private LocalDate dueDate;
    private Priority priority;
    public TaskRequest() {
    }
    public TaskRequest(String title, long taskListId, LocalDate dueDate, Priority priority) {
        this.title = title;
        this.taskListId = taskListId;
        this.dueDate = dueDate;
        this.priority = priority;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public long getTaskListId() {
        return taskListId;
    }
    public void setTaskListId(long taskListId) {
        this.taskListId = taskListId;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    public Priority getPriority() {
        return priority;
    }
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
}
