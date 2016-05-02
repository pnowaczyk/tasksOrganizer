package com.taskOrganizer.model;

/**
 * Created by Gosia on 2016-04-26.
 */
public class TaskModel {
    private String name;
    private String id;
    private Boolean isDone;

    public TaskModel(String name, String id) {
        this.name = name;
        this.id = id;
        this.isDone = false;
    }

    public TaskModel() {
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

    public Boolean getDone() {
        return isDone;
    }

    public void setDone(Boolean done) {
        isDone = done;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskModel taskModel = (TaskModel) o;
        if (name == null) {
            if (taskModel.name != null)
                return false;
        } else if (!name.equals(taskModel.name))
            return false;
        if (id == null) {
            if (taskModel.id != null)
                return false;
        } else if (!id.equals(taskModel.id))
            return false;
        return true;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

}
