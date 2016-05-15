package com.taskOrganizer.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import java.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.WebApplicationException;
import java.io.File;
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
    private String[] taskNames = {"ATech Leaders meeting", "BGoing shopping", "CJOGA class"};
    private String[] taskDescriptions = {"Tech Leaders meeting description", "Buying milk, eggs, tomatoes", "JOGA class, meeting with Magda"};
    private Client elasticClient;
    private File tmpDir;
    private Node node;
    private List<TaskModel> intialTaskList;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
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
        intialTaskList = new ArrayList<>();
        for (int i = 0; i < taskNames.length; i++) {
            taskInputData.name = taskNames[i];
            taskInputData.description = taskDescriptions[i];
            taskInputData.dueDate = Instant.now().plusMillis(1000*60*60*24);
            taskInputData.userName = "janTest";
            String createdTaskJSON = taskEndpoint.createTask(mapper.writeValueAsString(taskInputData));
            TaskModel createdTask = mapper.readValue(createdTaskJSON, TaskModel.class);
            intialTaskList.add(createdTask);
        }

        elasticClient.admin().indices().refresh(new RefreshRequest()).actionGet();
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
        taskInputData.name = "New task";
        taskInputData.description = "Brand new test task";
        taskInputData.dueDate = Instant.now().plusMillis(1000*60*60*24);
        taskInputData.userName = "gosiaTest";
        String createdTaskJSON = taskEndpoint.createTask(mapper.writeValueAsString(taskInputData));
        assertThat(repository.findAll().size()).isEqualTo(4);
        TaskModel createdTask = mapper.readValue(createdTaskJSON, TaskModel.class);
        assertThat(repository.findById(createdTask.getId())).isEqualTo(createdTask);
        GetResponse taskInElastic = elasticClient.prepareGet(
                "tasksorganizer", "task",
                createdTask.getId()).get();
        assertThat(mapper.readValue(taskInElastic.getSourceAsString(), TaskModel.class)).isEqualTo(createdTask);

    }

    @Test
    public void getAllTasks() throws Exception {


        String retrievedTasksJSON = taskEndpoint.getTasks("janTest");
        TaskModel[] retrievedTasks = mapper.readValue(retrievedTasksJSON, TaskModel[].class);
        List<TaskModel> retrievedTasksList = Arrays.asList(retrievedTasks);
        assertThat(retrievedTasksList.size()).isEqualTo(intialTaskList.size());
        assertThat(retrievedTasksList).isEqualTo(intialTaskList);
    }

    @Test
    public void findTasks() throws Exception {
        String foundTasksJSON = taskEndpoint.findTasks("Leaders", "janTest");
        TaskModel[] foundTasks = mapper.readValue(foundTasksJSON, TaskModel[].class);
        List<TaskModel> retrievedTasksList = Arrays.asList(foundTasks);
        assertThat(retrievedTasksList.size()).isEqualTo(1);
    }

    @Test
    public void markTaskDone() throws Exception {
        String markedDoneTaskJSON = taskEndpoint.markTaskDone(intialTaskList.get(0).getId(), "janTest");
        intialTaskList.get(0).setDone(true);
        TaskModel markedDoneTask = mapper.readValue(markedDoneTaskJSON, TaskModel.class);
        assertThat(intialTaskList.get(0)).isEqualTo(markedDoneTask);
    }

    @Test
    public void taskToMarkDoneDoesNotExist() {
        UUID uuid = UUID.randomUUID();
        assertThatThrownBy(() -> {
            taskEndpoint.markTaskDone(uuid.toString(), "janTest");
        }).isInstanceOf(WebApplicationException.class);
    }

    @Test
    public void getNotDoneTasks() throws Exception {
        String retrievedTasksJSON = taskEndpoint.getNotDoneTasks("janTest");
        TaskModel[] retrievedTasks = mapper.readValue(retrievedTasksJSON, TaskModel[].class);
        List<TaskModel> retrievedTasksList = Arrays.asList(retrievedTasks);
        assertThat(retrievedTasksList.size()).isEqualTo(3);
    }

    @Test
    public void taskEndpointIntegrationTest() throws Exception {
        //testing endpoint creating task
        taskInputData.name = "New task";
        taskInputData.description = "Brand new test task";
        taskInputData.dueDate = Instant.now();
        taskInputData.userName = "gosiaTest";
        String createdTaskJSON = taskEndpoint.createTask(mapper.writeValueAsString(taskInputData));
        elasticClient.admin().indices().refresh(new RefreshRequest()).actionGet();
        assertThat(repository.findAll().size()).isEqualTo(4);
        TaskModel createdTask = mapper.readValue(createdTaskJSON, TaskModel.class);
        TaskModel newTask = new TaskModel(taskInputData.name, taskInputData.description, createdTask.getId(), taskInputData.dueDate, taskInputData.userName);
        assertThat(newTask).isEqualTo(createdTask);

        //testing endpoint getting task list
        String retrievedTasksJSON = taskEndpoint.getTasks("janTest");
        TaskModel[] retrievedTasks = mapper.readValue(retrievedTasksJSON, TaskModel[].class);
        List<TaskModel> retrievedTasksList = Arrays.asList(retrievedTasks);
        assertThat(retrievedTasksList.size()).isEqualTo(3);
        assertThat(retrievedTasksList).isEqualTo(intialTaskList);

        //testing endpoint marking task done
        assertThat(createdTask.getDone()).isEqualTo(false);
        String markedDoneTaskJSON = taskEndpoint.markTaskDone(createdTask.getId(), "gosiaTest");
        TaskModel markedDoneTask = mapper.readValue(markedDoneTaskJSON, TaskModel.class);
        assertThat(markedDoneTask.getDone()).isEqualTo(true);
        assertThat(repository.findById(markedDoneTask.getId()).getDone()).isEqualTo(true);


    }
}