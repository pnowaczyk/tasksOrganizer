package com.taskOrganizer.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Gosia on 2016-04-26.
 */
@Path("/")
public interface TaskOrganizerEndpoint {

    @POST
    @Path("/newTask")
    String createTask() throws Exception;

    @GET
    @Path("/tasks")
    @Produces(MediaType.APPLICATION_JSON)
    String getTasks() throws Exception;

}
