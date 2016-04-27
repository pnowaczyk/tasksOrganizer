package com.taskOrganizer.model;

/**
 * Created by Gosia on 2016-04-26.
 */
public class TaskModel {
    private String name;
    private String id;

    public TaskModel(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }
}
