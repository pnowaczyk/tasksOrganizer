package com.taskOrganizer.conf;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Gosia on 2016-05-22.
 */

@Configuration
public class MessagingConf {

    private static final String EXCHANGE_NAME = "logs";

    @Bean(destroyMethod = "close")
    public Channel getMessagingChannel() throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        return channel;
    }
}
