package com.taskOrganizer.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrganizer.model.TaskListModel;
import com.taskOrganizer.model.TaskModel;
import com.taskOrganizer.model.TaskPostJSONModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by Gosia on 2016-04-30.
 */
public class TaskOrganizerEndpointImplTest {

    private TaskOrganizerEndpointImpl taskEndpoint;
    private TaskListModel taskListModel;
    private ObjectMapper mapper;
    private TaskPostJSONModel taskInputData;
    private String[] taskNames = {"Tech Leaders meeting", "Going shopping", "JOGA class"};

    @Before
    public void setUp() {
        taskListModel = new TaskListModel();
        mapper = new ObjectMapper();
        taskEndpoint = new TaskOrganizerEndpointImpl(taskListModel, mapper);
        taskInputData = new TaskPostJSONModel();
    }

    @After
    public void tearDown() {

        taskListModel.emptyTaskList();
    }

    @Test
    public void createNewTask() throws Exception {
        taskInputData.name = taskNames[0];
        String createdTaskJSON = taskEndpoint.createTask(mapper.writeValueAsString(taskInputData));
        assertThat(taskListModel.getTaskList().size()).isEqualTo(1);
        TaskModel createdTask = mapper.readValue(createdTaskJSON, TaskModel.class);
        assertThat(taskListModel.getTaskList().get(0)).isEqualTo(createdTask);

    }

    @Test
    public void getAllTasks() throws Exception {

        for (int i = 0; i < taskNames.length; i++) {
            taskInputData.name = taskNames[i];
            taskEndpoint.createTask(mapper.writeValueAsString(taskInputData));
        }
        String retrievedTasksJSON = taskEndpoint.getTasks();
        TaskModel[] retrievedTasks = mapper.readValue(retrievedTasksJSON, TaskModel[].class);
        List<TaskModel> retrievedTasksList = Arrays.asList(retrievedTasks);
        assertThat(retrievedTasksList.size()).isEqualTo(3);
        assertThat(retrievedTasksList).isEqualTo(taskListModel.getTaskList());
    }

    @Test
    public void markTaskDone() throws Exception {
        taskInputData.name = taskNames[0];
        String createdTaskJSON = taskEndpoint.createTask(mapper.writeValueAsString(taskInputData));
        TaskModel createdTask = mapper.readValue(createdTaskJSON, TaskModel.class);
        assertThat(createdTask.getDone()).isEqualTo(false);
        String markedDoneTaskJSON = taskEndpoint.markTaskDone(createdTask.getId());
        TaskModel markedDoneTask = mapper.readValue(markedDoneTaskJSON, TaskModel.class);
        assertThat(createdTask).isEqualTo(markedDoneTask);
        assertThat(markedDoneTask.getDone()).isEqualTo(true);
    }

    public void taskToMarkDoneDoesNotExist() {
        UUID uuid = UUID.randomUUID();
        assertThatThrownBy(() -> {
            taskEndpoint.markTaskDone(uuid.toString());
        }).isInstanceOf(WebApplicationException.class);
    }

    @Test
    public void taskEndpointIntegrationTest() throws Exception {
        //testing endpoint creating task
        taskInputData.name = taskNames[0];
        String createdTaskJSON = taskEndpoint.createTask(mapper.writeValueAsString(taskInputData));
        assertThat(taskListModel.getTaskList().size()).isEqualTo(1);
        TaskModel createdTask = mapper.readValue(createdTaskJSON, TaskModel.class);
        assertThat(taskListModel.getTaskList().get(0)).isEqualTo(createdTask);

        //testing endpoint marking task done
        assertThat(createdTask.getDone()).isEqualTo(false);
        String markedDoneTaskJSON = taskEndpoint.markTaskDone(createdTask.getId());
        TaskModel markedDoneTask = mapper.readValue(markedDoneTaskJSON, TaskModel.class);
        assertThat(createdTask).isEqualTo(markedDoneTask);
        assertThat(markedDoneTask.getDone()).isEqualTo(true);

        //testing endpoint getting task list
        taskInputData.name = taskNames[1];
        taskEndpoint.createTask(mapper.writeValueAsString(taskInputData));
        taskInputData.name = taskNames[2];
        taskEndpoint.createTask(mapper.writeValueAsString(taskInputData));
        String retrievedTasksJSON = taskEndpoint.getTasks();
        TaskModel[] retrievedTasks = mapper.readValue(retrievedTasksJSON, TaskModel[].class);
        List<TaskModel> retrievedTasksList = Arrays.asList(retrievedTasks);
        assertThat(retrievedTasksList.size()).isEqualTo(3);
        assertThat(retrievedTasksList).isEqualTo(taskListModel.getTaskList());
    }
}