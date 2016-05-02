package com.taskOrganizer.model;

import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gosia on 2016-04-26.
 */

@Configuration
public class TaskListModel {
    public static List<TaskModel> taskList;

    static {
        taskList = new ArrayList<>();
    }


    public List<TaskModel> getTaskList() {

        return taskList;
    }

    public void emptyTaskList() {

        taskList.clear();
    }

    public TaskModel getTaskById(String id) {
        TaskModel foundTask = null;
        for (TaskModel task : taskList) {
            if (task.getId().equals(id)) {
                foundTask = task;
                break;
            }
        }
        return foundTask;
    }

}
