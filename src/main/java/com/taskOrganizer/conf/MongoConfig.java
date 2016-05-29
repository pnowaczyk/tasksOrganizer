package com.taskOrganizer.conf;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Created by Gosia on 2016-05-03.
 */

@Configuration
@EnableMongoRepositories(basePackages = "com.taskOrganizer.model")
@ConfigurationProperties(locations = "classpath:mongoDB.properties", prefix = "mongo")
public class MongoConfig extends AbstractMongoConfiguration {
    private String databaseName;
    private String dbPort;
    private String mappingBasePackage;

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;

    }

    public String getDbPort() {
        return dbPort;
    }

    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }

    public void setMappingBasePackage(String mappingBasePackage) {
        this.mappingBasePackage = mappingBasePackage;
    }

    @Override
    public Mongo mongo() throws Exception {
        return new MongoClient(dbPort, 27017);
    }

    @Override
    public String getMappingBasePackage() {
        return "com.taskOrganizer";
    }
}
