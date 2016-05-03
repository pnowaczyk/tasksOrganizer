package com.taskOrganizer.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrganizer.model.TaskModel;
import com.taskOrganizer.model.TaskPostJSONModel;
import com.taskOrganizer.model.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.WebApplicationException;
import java.util.UUID;


/**
 * Created by Gosia on 2016-04-26.
 */
@EndpointImplementation
public class TaskOrganizerEndpointImpl implements TaskOrganizerEndpoint {

    private ObjectMapper mapper;
    private TaskRepository repository;

    @Autowired
    public TaskOrganizerEndpointImpl(ObjectMapper mapper, TaskRepository repository) {

        this.mapper = mapper;
        this.repository = repository;
    }

    public String createTask(String inputData) throws Exception {
        TaskPostJSONModel inputDataObj = mapper.readValue(inputData, TaskPostJSONModel.class);
        TaskModel task = new TaskModel(inputDataObj.name, UUID.randomUUID().toString());
        repository.save(task);
        return mapper.writeValueAsString(task);
    }

    public String getTasks() throws Exception {
        return mapper.writeValueAsString(repository.findAll());
    }

    @Override
    public String markTaskDone(String taskId) throws Exception {
        TaskModel foundTask = repository.findById(taskId);
        if (foundTask != null) {
            foundTask.setDone(true);
            repository.save(foundTask);
            return mapper.writeValueAsString(foundTask);
        } else {
            throw new WebApplicationException(404);
        }

    }
}
