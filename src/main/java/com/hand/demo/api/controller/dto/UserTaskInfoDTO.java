package com.hand.demo.api.controller.dto;

import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class UserTaskInfoDTO extends User {
    private String taskNumber;
    private List<Task> tasks;

    public UserTaskInfoDTO() {

    }// search in your web site (parameter) -> your service -> result -> web site

}
