package com.taskOrganizer.conf;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Created by Gosia on 2016-06-04.
 */

@Configuration
public class MessagingConnectionConfig {

    @Bean(destroyMethod = "close")
    public Connection getMessagingConnection() throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        return connection;
    }

}
