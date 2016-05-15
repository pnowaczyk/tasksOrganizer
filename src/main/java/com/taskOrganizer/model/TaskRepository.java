package com.taskOrganizer.model;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by Gosia on 2016-05-02.
 */
public interface TaskRepository extends MongoRepository<TaskModel, String> {
    public TaskModel findById(String id);
    public List<TaskModel> findByName(String name);
}
