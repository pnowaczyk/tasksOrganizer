package com.taskOrganizer.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrganizer.model.TaskListModel;
import com.taskOrganizer.model.TaskModel;
import com.taskOrganizer.model.TaskPostJSONModel;
import org.springframework.beans.factory.annotation.Autowired;
import javax.ws.rs.WebApplicationException;
import java.util.UUID;


/**
 * Created by Gosia on 2016-04-26.
 */
@EndpointImplementation
public class TaskOrganizerEndpointImpl implements TaskOrganizerEndpoint {

    private TaskListModel tasksListModel;
    private ObjectMapper mapper;

    @Autowired
    public TaskOrganizerEndpointImpl(TaskListModel tasksListModel, ObjectMapper mapper) {

        this.tasksListModel = tasksListModel;
        this.mapper = mapper;
    }

    public String createTask(String inputData) throws Exception {
        TaskPostJSONModel inputDataObj = mapper.readValue(inputData, TaskPostJSONModel.class);
        TaskModel task = new TaskModel(inputDataObj.name, UUID.randomUUID().toString());
        tasksListModel.getTaskList().add(task);
        return mapper.writeValueAsString(task);
    }

    public String getTasks() throws Exception {
        return mapper.writeValueAsString( tasksListModel.getTaskList());
    }

    @Override
    public String markTaskDone(String taskId) throws Exception {
        TaskModel foundTask = tasksListModel.getTaskById(taskId);
        if (foundTask != null) {
            foundTask.setDone(true);
            return mapper.writeValueAsString(foundTask);
        } else {
            throw new WebApplicationException(404);
        }

    }
}
