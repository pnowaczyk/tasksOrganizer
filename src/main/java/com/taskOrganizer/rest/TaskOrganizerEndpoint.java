package com.taskOrganizer.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by Gosia on 2016-04-26.
 */
@Path("/")
public interface TaskOrganizerEndpoint {

    @POST
    @Consumes({"application/json"})
    @Path("/newTask")
    String createTask(String inputData) throws Exception;

    @GET
    @Path("/tasks")
    @Produces(MediaType.APPLICATION_JSON)
    String getTasks() throws Exception;

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    String findTasks(
            @QueryParam("query") String query
    ) throws Exception;

    @POST
    @Path("/done/{taskId}")
    String markTaskDone(@PathParam("taskId") String taskId) throws Exception;

    @GET
    @Path("/tasks/notDone")
    @Produces(MediaType.APPLICATION_JSON)
    String getNotDoneTasks() throws Exception;
}
