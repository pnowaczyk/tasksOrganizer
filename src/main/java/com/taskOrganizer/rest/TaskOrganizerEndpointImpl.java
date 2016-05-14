package com.taskOrganizer.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskOrganizer.model.TaskModel;
import com.taskOrganizer.model.TaskPostJSONModel;
import com.taskOrganizer.model.TaskRepository;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;


/**
 * Created by Gosia on 2016-04-26.
 */
@EndpointImplementation
public class TaskOrganizerEndpointImpl implements TaskOrganizerEndpoint {

    private ObjectMapper mapper;
    private TaskRepository repository;
    private Client elasticClient;
    final static Logger logger = LoggerFactory.getLogger(TaskOrganizerEndpointImpl.class);

    @Autowired
    public TaskOrganizerEndpointImpl(ObjectMapper mapper, TaskRepository repository, Client elasticClient) {

        this.mapper = mapper;
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        this.repository = repository;
        this.elasticClient = elasticClient;
    }

    public String createTask(String inputData) throws Exception {
        logger.info("Creating task");
        TaskPostJSONModel inputDataObj = mapper.readValue(inputData, TaskPostJSONModel.class);
        TaskModel task = new TaskModel(inputDataObj.name, inputDataObj.description, UUID.randomUUID().toString(), inputDataObj.dueDate, inputDataObj.userName);
        repository.save(task);
        IndexResponse response = elasticClient.prepareIndex("tasksorganizer", "task", task.getId())
                .setSource(mapper.writeValueAsBytes(task)).get();
        return mapper.writeValueAsString(task);
    }

    public String getTasks(String user) throws Exception {
        logger.info("Getting tasks for user " + user);
        QueryBuilder qb = QueryBuilders
                .boolQuery()
                .must(matchQuery("userName", user));

        SearchResponse res = elasticClient.prepareSearch("tasksorganizer")
                .setTypes("task")
                .setQuery(qb).addSort("name", SortOrder.ASC).execute()
                .actionGet();
        List<TaskModel> allTasks = new ArrayList<>();
        Iterator<SearchHit> iterator = res.getHits().iterator();
        while (iterator.hasNext()) {
            allTasks.add(mapper.readValue(iterator.next().getSourceAsString(), TaskModel.class));
        }
        logger.info("Nb of found tasks for user " + user + " - " + allTasks.size());
        logger.info("Found tasks for user " + user + " - " + allTasks);
        return mapper.writeValueAsString(allTasks);
    }


    @Override
    public String findTasks(String query, String user) throws Exception {
        logger.info("Searching tasks for user " + user + " with query: " + query);
        QueryBuilder qb = QueryBuilders
                .boolQuery()
                .must(matchQuery("userName", user))
                .should(matchQuery("name", query))
                .should(matchQuery("description", query )).minimumNumberShouldMatch(1);
        SearchResponse res = elasticClient.prepareSearch("tasksorganizer")
                .setTypes("task")
                .setQuery(qb).addSort("name", SortOrder.ASC).execute()
                .actionGet();

        //SimpleQueryStringBuilder sb = QueryBuilders.simpleQueryStringQuery(query);
        List<TaskModel> foundTasks = new ArrayList<>();
        Iterator<SearchHit> iterator = res.getHits().iterator();
        while (iterator.hasNext()) {
            foundTasks.add(mapper.readValue(iterator.next().getSourceAsString(), TaskModel.class));
        }


        return mapper.writeValueAsString(foundTasks);
    }

    //@Retryable -> do ponawiania transakcji i w aplikacji trzeba dodać enableRetryable
    @Override
    public String markTaskDone(String taskId, String user) throws Exception {
        logger.info("Marking task done with taskId: " + taskId + " for user " + user);
        TaskModel foundTaskInDb = repository.findById(taskId);
        TaskModel foundTask = null;
        QueryBuilder qb = QueryBuilders
                .boolQuery()
                .must(matchQuery("userName", user))
                .must(matchQuery("id", taskId));

        SearchResponse res = elasticClient.prepareSearch("tasksorganizer")
                .setTypes("task")
                .setQuery(qb).execute()
                .actionGet();
        if(res.getHits().hits().length == 1){
            Iterator<SearchHit> iterator = res.getHits().iterator();
            if (iterator.hasNext()) {
                foundTask = mapper.readValue(iterator.next().getSourceAsString(), TaskModel.class);
            }
        }
        else{
            throw new WebApplicationException(404);
        }
        if (foundTaskInDb != null && (foundTask != null)) {
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
    public String getNotDoneTasks(String user) throws Exception {
        logger.info("Getting not done tasks for user " + user);
        List<TaskModel> foundTasks = new ArrayList<>();
        QueryBuilder qb = QueryBuilders
                .boolQuery()
                .must(matchQuery("userName", user))
                .must(matchQuery("done", false));

        SearchResponse res = elasticClient.prepareSearch("tasksorganizer")
                .setTypes("task")
                .setQuery(qb).execute()
                .actionGet();



        SearchResponse res2 = elasticClient.prepareSearch("tasksorganizer").setTypes("task").addSort("dueDate", SortOrder.ASC).setQuery(qb).execute().actionGet();
        Iterator<SearchHit> iterator2 = res2.getHits().iterator();
        while (iterator2.hasNext()) {
            foundTasks.add(mapper.readValue(iterator2.next().getSourceAsString(), TaskModel.class));
        }
        return mapper.writeValueAsString(foundTasks);
    }
}
