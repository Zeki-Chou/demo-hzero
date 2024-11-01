package com.hand.demo.domain.dto;

import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserTaskDTO extends User {
    private String taskNumber;
    private List<Task> listTask;

}
