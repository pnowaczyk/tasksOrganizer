package com.taskOrganizer.rest;


import com.taskOrganizer.model.TaskListModel;
import com.taskOrganizer.model.TaskModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

/**
 * Created by Gosia on 2016-04-26.
 */
@EndpointImplementation
public class TaskOrganizerEndpointImpl implements TaskOrganizerEndpoint {

    private TaskListModel tasksList;

    @Autowired
    public TaskOrganizerEndpointImpl(TaskListModel tasksList) {
        this.tasksList = tasksList;
    }

    public String createTask() throws Exception {
        TaskModel task = new TaskModel("TaskName", UUID.randomUUID().toString());
        TaskListModel.taskList.add(task);
        return task.getName();
    }

    public String getTasks() throws Exception {
        return TaskListModel.taskList.toString();
    }
}
