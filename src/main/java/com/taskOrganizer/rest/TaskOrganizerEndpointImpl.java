package com.taskOrganizer.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrganizer.model.TaskListModel;
import com.taskOrganizer.model.TaskModel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import java.util.UUID;

import static com.taskOrganizer.model.TaskListModel.taskList;

/**
 * Created by Gosia on 2016-04-26.
 */
@EndpointImplementation
public class TaskOrganizerEndpointImpl implements TaskOrganizerEndpoint {

    private TaskListModel tasksListModel;

    @Autowired
    public TaskOrganizerEndpointImpl(TaskListModel tasksListModel) {
        this.tasksListModel = tasksListModel;
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

    @Override
    public String markTaskDone(String taskId) throws Exception {
        TaskModel foundTask = null;
        for (TaskModel task : taskList) {
            if (task.getId().equals(taskId)) {
                foundTask = task;
                break;
            }
        }
        if (foundTask != null) {
            foundTask.setDone(true);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(foundTask);
        } else {
            throw new WebApplicationException(404);
        }

    }
}
