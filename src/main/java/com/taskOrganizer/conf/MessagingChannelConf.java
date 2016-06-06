package com.taskOrganizer.conf;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Gosia on 2016-05-22.
 */

@Configuration
public class MessagingChannelConf {

    private static final String EXCHANGE_NAME = "logs";

    @Autowired
    @Bean(destroyMethod = "close")
    public Channel getMessagingChannel(Connection messagingConnection) throws Exception {
        Channel channel = messagingConnection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        return channel;
    }
}
