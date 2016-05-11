package com.taskOrganizer.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskOrganizer.model.TaskModel;
import com.taskOrganizer.model.TaskPostJSONModel;
import com.taskOrganizer.model.TaskRepository;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.WebApplicationException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


/**
 * Created by Gosia on 2016-04-26.
 */
@EndpointImplementation
public class TaskOrganizerEndpointImpl implements TaskOrganizerEndpoint {

    private ObjectMapper mapper;
    private TaskRepository repository;
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private Client elasticClient;
    final static Logger logger = LoggerFactory.getLogger(TaskOrganizerEndpointImpl.class);

    @Autowired
    public TaskOrganizerEndpointImpl(ObjectMapper mapper, TaskRepository repository, Client elasticClient) {

        this.mapper = mapper;
        mapper.registerModule(new JavaTimeModule());
        this.repository = repository;
        this.elasticClient = elasticClient;
    }

    public String createTask(String inputData) throws Exception {
        logger.info("Creating task");
        TaskPostJSONModel inputDataObj = mapper.readValue(inputData, TaskPostJSONModel.class);
        TaskModel task = new TaskModel(inputDataObj.name, inputDataObj.description, UUID.randomUUID().toString(), inputDataObj.dueDate);
        repository.save(task);
        IndexResponse response = elasticClient.prepareIndex("tasksorganizer", "task", task.getId())
                .setSource(mapper.writeValueAsBytes(task)).get();
        return mapper.writeValueAsString(task);
    }

    public String getTasks() throws Exception {
        logger.info("Getting tasks");
        SearchResponse res = elasticClient.prepareSearch("tasksorganizer")
                .setTypes("task")
                .setQuery(QueryBuilders.matchAllQuery()).execute()
                .actionGet();
        List<TaskModel> allTasks = new ArrayList<>();
        Iterator<SearchHit> iterator = res.getHits().iterator();
        while (iterator.hasNext()) {
            allTasks.add(mapper.readValue(iterator.next().getSourceAsString(), TaskModel.class));
        }
        return mapper.writeValueAsString(allTasks);
    }


    @Override
    public String findTasks(String query) throws Exception {
        logger.info("Searching tasks with query: " + query);
        //SimpleQueryStringBuilder sb = QueryBuilders.simpleQueryStringQuery(query);
        QueryBuilder qb = QueryBuilders.wildcardQuery("name", query);
        SearchResponse res = elasticClient.prepareSearch("tasksorganizer").setTypes("task").setQuery(qb).execute().actionGet();
        Set<TaskModel> foundTasks = new HashSet<>();
        Iterator<SearchHit> iterator = res.getHits().iterator();
        while (iterator.hasNext()) {
            foundTasks.add(mapper.readValue(iterator.next().getSourceAsString(), TaskModel.class));
        }

        QueryBuilder qb2 = QueryBuilders.wildcardQuery("description", query);
        SearchResponse res2 = elasticClient.prepareSearch("tasksorganizer").setTypes("task").setQuery(qb2).execute().actionGet();
        Iterator<SearchHit> iterator2 = res2.getHits().iterator();
        while (iterator2.hasNext()) {
            foundTasks.add(mapper.readValue(iterator2.next().getSourceAsString(), TaskModel.class));
        }


        return mapper.writeValueAsString(foundTasks);
    }

    //@Retryable -> do ponawiania transakcji i w aplikacji trzeba dodać enableRetryable
    @Override
    public String markTaskDone(String taskId) throws Exception {
        logger.info("Marking task done with taskId: " + taskId);
        GetResponse getResponse = elasticClient
        .prepareGet("tasksorganizer", "task", taskId).get();
        TaskModel foundTask = repository.findById(taskId);

        if (getResponse.isExists() && (foundTask != null)) {
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.index("tasksorganizer");
            updateRequest.type("task");
            updateRequest.id(taskId);
            updateRequest.doc(jsonBuilder()
                    .startObject()
                    .field("done", true)
                    .endObject());
            elasticClient.update(updateRequest).get();
            foundTask.setDone(true);
            repository.save(foundTask);
            return mapper.writeValueAsString(foundTask);


        } else {
            throw new WebApplicationException(404);
        }

    }

    @Override
    public String getNotDoneTasks() throws Exception {
        logger.info("Getting not done tasks ");
        List<TaskModel> foundTasks = new ArrayList<>();
        QueryBuilder qb = QueryBuilders.matchQuery("done", false);

        SearchResponse res2 = elasticClient.prepareSearch("tasksorganizer").setTypes("task").addSort("dueDate", SortOrder.ASC).setQuery(qb).execute().actionGet();
        Iterator<SearchHit> iterator2 = res2.getHits().iterator();
        while (iterator2.hasNext()) {
            foundTasks.add(mapper.readValue(iterator2.next().getSourceAsString(), TaskModel.class));
        }
        return mapper.writeValueAsString(foundTasks);
    }
}
