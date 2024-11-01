package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDTO extends User {
    private String taskNumber;
    private List<Task> taskList;
}
