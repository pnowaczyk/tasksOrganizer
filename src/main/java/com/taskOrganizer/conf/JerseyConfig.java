package com.taskOrganizer.conf;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.taskOrganizer.rest.EndpointImplementation;

import javax.ws.rs.ApplicationPath;

@Component
@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {

    @Autowired
    public JerseyConfig(@EndpointImplementation Object[] endpointImplementations) {
        registerInstances(endpointImplementations);
    }
}
