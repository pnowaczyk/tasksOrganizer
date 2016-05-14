package com.taskOrganizer.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

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
    @Path("/tasks/{user}")
    @Produces(MediaType.APPLICATION_JSON)
    String getTasks(
            @PathParam("user") String user
    ) throws Exception;

    @GET
    @Path("/search/{user}")
    @Produces(MediaType.APPLICATION_JSON)
    String findTasks(
            @QueryParam("query") String query, @PathParam("user") String user
    ) throws Exception;

    @POST
    @Path("/done/{taskId}/{user}")
    String markTaskDone(@PathParam("taskId") String taskId, @PathParam("user") String user) throws Exception;

    @GET
    @Path("/tasks/notDone/{user}")
    @Produces(MediaType.APPLICATION_JSON)
    String getNotDoneTasks(@PathParam("user") String user) throws Exception;
}
