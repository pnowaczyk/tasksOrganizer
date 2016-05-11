package com.taskOrganizer.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.io.Files;
import com.taskOrganizer.conf.MongoTestConfig;
import com.taskOrganizer.model.TaskModel;
import com.taskOrganizer.model.TaskPostJSONModel;
import com.taskOrganizer.model.TaskRepository;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.WebApplicationException;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Created by Gosia on 2016-04-30.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MongoTestConfig.class)
public class TaskOrganizerEndpointImplTest {

    private TaskOrganizerEndpointImpl taskEndpoint;
    private ObjectMapper mapper;
    private TaskPostJSONModel taskInputData;
    @Autowired
    private TaskRepository repository;
    private String[] taskNames = {"Tech Leaders meeting", "Going shopping", "JOGA class"};
    private String[] taskDescriptions= {"Tech Leaders meeting description", "Buying milk, eggs, tomatoes", "JOGA class, meeting with Magda"};
    private Client elasticClient;
    private File tmpDir;
    private Node node;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        tmpDir = Files.createTempDir();
        node = nodeBuilder()
                .local(true)
                .settings(
                        Settings.builder().put("path.home",
                                tmpDir.getAbsolutePath()))
                .clusterName("taskOrganizerCluster").node();
        elasticClient = node.client();
        // documents for tests
        elasticClient.admin().indices().create(Requests.createIndexRequest("tasksorganizer")).actionGet();
        elasticClient.admin().indices().refresh(new RefreshRequest()).actionGet();
        taskEndpoint = new TaskOrganizerEndpointImpl(mapper, repository, elasticClient);
        taskInputData = new TaskPostJSONModel();
        repository.deleteAll();
    }

    @After
    public void tearDown() {

        elasticClient.admin()
                .indices()
                .delete(Requests
                        .deleteIndexRequest("tasksorganizer"))
                .actionGet();
        node.close();
        tmpDir.delete();
    }

    @Test
    public void createNewTask() throws Exception {
        taskInputData.name = taskNames[0];
        taskInputData.description = taskDescriptions[0];
        taskInputData.dueDate = LocalDateTime.now().plusDays(7);
        String createdTaskJSON = taskEndpoint.createTask(mapper.writeValueAsString(taskInputData));
        assertThat(repository.findAll().size()).isEqualTo(1);
        TaskModel createdTask = mapper.readValue(createdTaskJSON, TaskModel.class);
        assertThat(repository.findById(createdTask.getId())).isEqualTo(createdTask);
        GetResponse taskInElastic = elasticClient.prepareGet(
                "tasksorganizer", "task",
                createdTask.getId()).get();
        assertThat(mapper.readValue(taskInElastic.getSourceAsString(), TaskModel.class)).isEqualTo(createdTask);

    }

    @Test
    public void getAllTasks() throws Exception {

        for (int i = 0; i < taskNames.length; i++) {
            taskInputData.name = taskNames[i];
            taskInputData.description = taskDescriptions[i];
            taskInputData.dueDate = LocalDateTime.now().plusDays(7 + i);
            taskEndpoint.createTask(mapper.writeValueAsString(taskInputData));
        }
        String retrievedTasksJSON = taskEndpoint.getTasks();
        TaskModel[] retrievedTasks = mapper.readValue(retrievedTasksJSON, TaskModel[].class);
        List<TaskModel> retrievedTasksList = Arrays.asList(retrievedTasks);
        assertThat(retrievedTasksList.size()).isEqualTo(3);
        assertThat(retrievedTasksList).isEqualTo(repository.findAll());
    }

    @Test
    public void findTasks() throws Exception{
        for (int i = 0; i < taskNames.length; i++) {
            taskInputData.name = taskNames[i];
            taskInputData.description = taskDescriptions[i];
            taskInputData.dueDate = LocalDateTime.now().plusDays(7 + i);
            taskEndpoint.createTask(mapper.writeValueAsString(taskInputData));
        }
        String foundTasksJSON = taskEndpoint.findTasks("Leaders");
        TaskModel[] foundTasks = mapper.readValue(foundTasksJSON, TaskModel[].class);
        List<TaskModel> retrievedTasksList = Arrays.asList(foundTasks);
        assertThat(retrievedTasksList.size()).isEqualTo(1);
    }

    @Test
    public void markTaskDone() throws Exception {
        taskInputData.name = taskNames[0];
        taskInputData.description = taskDescriptions[0];
        taskInputData.dueDate = LocalDateTime.now().plusDays(7);
        String createdTaskJSON = taskEndpoint.createTask(mapper.writeValueAsString(taskInputData));
        TaskModel createdTask = mapper.readValue(createdTaskJSON, TaskModel.class);
        assertThat(createdTask.getDone()).isEqualTo(false);
        String markedDoneTaskJSON = taskEndpoint.markTaskDone(createdTask.getId());
        TaskModel markedDoneTask = mapper.readValue(markedDoneTaskJSON, TaskModel.class);
        assertThat(createdTask).isEqualTo(markedDoneTask);
        assertThat(markedDoneTask.getDone()).isEqualTo(true);
        GetResponse taskInElastic = elasticClient.prepareGet(
                "tasksorganizer", "task",
                createdTask.getId()).get();
        assertThat(mapper.readValue(taskInElastic.getSourceAsString(), TaskModel.class)).isEqualTo(createdTask);
        assertThat(taskInElastic.getSource().get("done")).isEqualTo(true);
    }

    @Test
    public void taskToMarkDoneDoesNotExist() {
        UUID uuid = UUID.randomUUID();
        assertThatThrownBy(() -> {
            taskEndpoint.markTaskDone(uuid.toString());
        }).isInstanceOf(WebApplicationException.class);
    }

    @Test
    public void getNotDoneTasks() throws Exception{
        List<String> uuids = new ArrayList<>();
        for (int i = 0; i < taskNames.length; i++) {
            taskInputData.name = taskNames[i];
            taskInputData.description = taskDescriptions[i];
            taskInputData.dueDate = LocalDateTime.now().plusDays(7 + i);
            String createdTaskJSON = taskEndpoint.createTask(mapper.writeValueAsString(taskInputData));
            TaskModel createdTask = mapper.readValue(createdTaskJSON, TaskModel.class);
            uuids.add(createdTask.getId());
        }

        String retrievedTasksJSON = taskEndpoint.getNotDoneTasks();
        TaskModel[] retrievedTasks = mapper.readValue(retrievedTasksJSON, TaskModel[].class);
        List<TaskModel> retrievedTasksList = Arrays.asList(retrievedTasks);
        assertThat(retrievedTasksList.size()).isEqualTo(3);
    }

    @Test
    public void taskEndpointIntegrationTest() throws Exception {
        //testing endpoint creating task
        taskInputData.name = taskNames[0];
        taskInputData.description = taskDescriptions[0];
        taskInputData.dueDate = LocalDateTime.now().plusDays(7);
        String createdTaskJSON = taskEndpoint.createTask(mapper.writeValueAsString(taskInputData));
        assertThat(repository.findAll().size()).isEqualTo(1);
        TaskModel createdTask = mapper.readValue(createdTaskJSON, TaskModel.class);
        assertThat(repository.findById(createdTask.getId())).isEqualTo(createdTask);

        //testing endpoint marking task done
        assertThat(createdTask.getDone()).isEqualTo(false);
        String markedDoneTaskJSON = taskEndpoint.markTaskDone(createdTask.getId());
        TaskModel markedDoneTask = mapper.readValue(markedDoneTaskJSON, TaskModel.class);
        assertThat(createdTask).isEqualTo(markedDoneTask);
        assertThat(markedDoneTask.getDone()).isEqualTo(true);

        //testing endpoint getting task list
        taskInputData.name = taskNames[1];
        taskInputData.description = taskDescriptions[1];
        taskInputData.dueDate = LocalDateTime.now().plusDays(7);
        taskEndpoint.createTask(mapper.writeValueAsString(taskInputData));
        taskInputData.name = taskNames[2];
        taskInputData.description = taskDescriptions[2];
        taskInputData.dueDate = LocalDateTime.now().plusDays(7);
        taskEndpoint.createTask(mapper.writeValueAsString(taskInputData));
        String retrievedTasksJSON = taskEndpoint.getTasks();
        TaskModel[] retrievedTasks = mapper.readValue(retrievedTasksJSON, TaskModel[].class);
        List<TaskModel> retrievedTasksList = Arrays.asList(retrievedTasks);
        assertThat(retrievedTasksList.size()).isEqualTo(3);
        assertThat(retrievedTasksList).isEqualTo(repository.findAll());
    }
}