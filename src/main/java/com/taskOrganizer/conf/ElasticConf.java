package com.taskOrganizer.conf;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Gosia on 2016-05-05.
 */

@Configuration
public class ElasticConf {

    @Bean(destroyMethod = "close")
    public Client getElasticClient() throws UnknownHostException {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "elasticsearch").build();

        Client client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
        return client;
    }
}
