package com.taskOrganizer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Created by Gosia on 2016-04-26.
 */
@Document(collection = "tasks")
public class TaskModel {
    private String name;
    @Id
    private String id;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    //można na Instant i timestamp
    private LocalDateTime dueDate;
    private Boolean isDone;

    public TaskModel(String name, String description, String id, LocalDateTime dueDate) {
        this.name = name;
        this.id = id;
        this.description = description;
        this.dueDate = dueDate;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
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
