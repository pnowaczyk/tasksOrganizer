package com.taskOrganizer.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrganizer.model.TaskListModel;
import com.taskOrganizer.model.TaskModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Gosia on 2016-04-30.
 */
public class TaskOrganizerEndpointImplTest {

    private TaskOrganizerEndpointImpl taskEndpoint;
    private TaskListModel taskListModel;

    @Before
    public void setUp() {
        taskListModel = new TaskListModel();
        taskEndpoint = new TaskOrganizerEndpointImpl(taskListModel);
    }

    @After
    public void tearDown() {
        taskListModel.emptyTaskList();
    }

    @Test
    public void createNewTask() throws Exception {
        assertThat(taskListModel.getTaskList()).isEmpty();
        String createdTaskJSON = taskEndpoint.createTask("Tech Leaders meeting");
        assertThat(taskListModel.getTaskList().size()).isEqualTo(1);
        ObjectMapper mapper = new ObjectMapper();
        TaskModel createdTask = mapper.readValue(createdTaskJSON, TaskModel.class);
        assertThat(taskListModel.getTaskList().get(0)).isEqualTo(createdTask);

    }

    @Test
    public void getAllTasks() throws Exception {
        assertThat(taskListModel.getTaskList()).isEmpty();
        taskEndpoint.createTask("Tech Leaders meeting");
        taskEndpoint.createTask("Going shopping");
        taskEndpoint.createTask("JOGA class");
        String retrievedTasksJSON = taskEndpoint.getTasks();
        ObjectMapper mapper = new ObjectMapper();
        TaskModel[] retrievedTasks = mapper.readValue(retrievedTasksJSON, TaskModel[].class);
        List<TaskModel> retrievedTasksList = Arrays.asList(retrievedTasks);
        assertThat(retrievedTasksList.size()).isEqualTo(3);
        assertThat(retrievedTasksList).isEqualTo(taskListModel.getTaskList());
    }

    @Test
    public void taskEndpointIntegrationTest() throws Exception{
        assertThat(taskListModel.getTaskList()).isEmpty();
        String createdTaskJSON = taskEndpoint.createTask("Tech Leaders meeting");
        assertThat(taskListModel.getTaskList().size()).isEqualTo(1);
        ObjectMapper mapper = new ObjectMapper();
        TaskModel createdTask = mapper.readValue(createdTaskJSON, TaskModel.class);
        assertThat(taskListModel.getTaskList().get(0)).isEqualTo(createdTask);
        taskEndpoint.createTask("Going shopping");
        taskEndpoint.createTask("JOGA class");
        String retrievedTasksJSON = taskEndpoint.getTasks();
        TaskModel[] retrievedTasks = mapper.readValue(retrievedTasksJSON, TaskModel[].class);
        List<TaskModel> retrievedTasksList = Arrays.asList(retrievedTasks);
        assertThat(retrievedTasksList.size()).isEqualTo(3);
        assertThat(retrievedTasksList).isEqualTo(taskListModel.getTaskList());
    }
}