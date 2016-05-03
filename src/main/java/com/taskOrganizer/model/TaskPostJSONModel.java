package com.taskOrganizer.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;

/**
 * Created by Gosia on 2016-05-02.
 */
@XmlRootElement
public class TaskPostJSONModel {
    @XmlElement
    public String name;
    public String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    public LocalDateTime dueDate;
}
