package com.taskOrganizer.rest;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrganizer.model.TaskListModel;
import com.taskOrganizer.model.TaskModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.UUID;

import static com.taskOrganizer.model.TaskListModel.taskList;

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

    public String createTask(String name) throws Exception {
        TaskModel task = new TaskModel(name, UUID.randomUUID().toString());
        taskList.add(task);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(task);
    }

    public String getTasks() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(taskList);
    }
}
