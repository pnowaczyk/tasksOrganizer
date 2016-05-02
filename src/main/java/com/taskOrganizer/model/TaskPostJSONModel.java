package com.taskOrganizer.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Gosia on 2016-05-02.
 */
@XmlRootElement
public class TaskPostJSONModel {
    @XmlElement
    public String name;
}
