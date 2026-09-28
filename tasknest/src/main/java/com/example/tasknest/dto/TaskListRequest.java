package com.example.tasknest.dto;

public class TaskListRequest {
 private String name;
 private String usetId;
 public TaskListRequest(){

 }
 public TaskListRequest(String name, String usetId){
    this.name=name;
    this.usetId=usetId;
 }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name=name;
    }
    public String getUsetId(){
        return usetId;
    }
    public void setUsetId(String usetId){
        this.usetId=usetId;
    }
}
