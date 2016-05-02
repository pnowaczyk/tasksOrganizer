package com.taskOrganizer.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskOrganizer.model.TaskListModel;
import com.taskOrganizer.model.TaskModel;
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
    public void markTaskDone() throws Exception {
        String createdTaskJSON = taskEndpoint.createTask("Tech Leaders meeting");
        ObjectMapper mapper = new ObjectMapper();
        TaskModel createdTask = mapper.readValue(createdTaskJSON, TaskModel.class);
        assertThat(createdTask.getDone()).isEqualTo(false);
        String markedDoneTaskJSON = taskEndpoint.markTaskDone(createdTask.getId());
        TaskModel markedDoneTask = mapper.readValue(markedDoneTaskJSON, TaskModel.class);
        assertThat(createdTask).isEqualTo(markedDoneTask);
        assertThat(markedDoneTask.getDone()).isEqualTo(true);
    }

    public void taskToMarkDoneDoesNotExist() {
        assertThat(taskListModel.getTaskList()).isEmpty();
        UUID uuid = UUID.randomUUID();
        assertThatThrownBy(() -> { taskEndpoint.markTaskDone(uuid.toString());
        }).isInstanceOf(WebApplicationException.class);
    }

    @Test
    public void taskEndpointIntegrationTest() throws Exception {
        //testing endpoint creating task
        assertThat(taskListModel.getTaskList()).isEmpty();
        String createdTaskJSON = taskEndpoint.createTask("Tech Leaders meeting");
        assertThat(taskListModel.getTaskList().size()).isEqualTo(1);
        ObjectMapper mapper = new ObjectMapper();
        TaskModel createdTask = mapper.readValue(createdTaskJSON, TaskModel.class);
        assertThat(taskListModel.getTaskList().get(0)).isEqualTo(createdTask);

        //testing endpoint marking task done
        assertThat(createdTask.getDone()).isEqualTo(false);
        String markedDoneTaskJSON = taskEndpoint.markTaskDone(createdTask.getId());
        TaskModel markedDoneTask = mapper.readValue(markedDoneTaskJSON, TaskModel.class);
        assertThat(createdTask).isEqualTo(markedDoneTask);
        assertThat(markedDoneTask.getDone()).isEqualTo(true);

        //testing endpoint getting task list
        taskEndpoint.createTask("Going shopping");
        taskEndpoint.createTask("JOGA class");
        String retrievedTasksJSON = taskEndpoint.getTasks();
        TaskModel[] retrievedTasks = mapper.readValue(retrievedTasksJSON, TaskModel[].class);
        List<TaskModel> retrievedTasksList = Arrays.asList(retrievedTasks);
        assertThat(retrievedTasksList.size()).isEqualTo(3);
        assertThat(retrievedTasksList).isEqualTo(taskListModel.getTaskList());
    }
}