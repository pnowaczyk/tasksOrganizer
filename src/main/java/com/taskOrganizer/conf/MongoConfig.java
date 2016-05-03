package com.taskOrganizer.conf;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Created by Gosia on 2016-05-03.
 */

@Configuration
@EnableMongoRepositories(basePackages = "com.taskOrganizer.model")
public class MongoConfig extends AbstractMongoConfiguration {


    @Override
    protected String getDatabaseName() {
        return "tasksDemoDb";
    }

    @Override
    public Mongo mongo() throws Exception {
        return new MongoClient("127.0.0.1", 27017);
    }

    @Override
    public String getMappingBasePackage() {
        return "com.taskOrganizer";
    }
}
