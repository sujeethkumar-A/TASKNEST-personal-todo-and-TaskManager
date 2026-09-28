package com.example.tasknest.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
@Entity 
@Table (name= "task_lists")
public class TaskList {
    @Id 
    @Generated Value(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @ManyToOne 
    @JoinColumn(name = "user_id")
    private User user;
    public TaskList(){

    }
    public TaskList(long id,String name, User user){
        this.id=id;
        this.name=name;
        this.user=user;
    }
    public long getId(){
        return id;

    }
    public void setId(long id){
        this.id=id;
    }
    public String getName(){
        return name;

    }
    public void setName(String name){
        this.name=name;
    }
    public User getUser(){
        return user;
    }
    public void setUser(User user){
        this.user=user;
    }
}
