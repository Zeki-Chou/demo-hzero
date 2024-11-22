package com.hand.demo.app.service.impl;

import com.hand.demo.api.controller.dto.UserTaskInfoDTO;
import com.hand.demo.app.service.UserTaskService;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.TaskRepository;
import com.hand.demo.domain.repository.UserRepository;
import com.hand.demo.infra.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserTaskInfoServiceImpl implements UserTaskService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public UserTaskInfoServiceImpl(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public UserTaskInfoDTO findUserTaskInfo(Long id) {
        User user = userRepository.selectByPrimaryKey(id);
        List<Task> tasks = taskRepository.select(Task.FIELD_EMPLOYEE_ID, id);
        UserTaskInfoDTO userTaskInfoDTO = new UserTaskInfoDTO();

        userTaskInfoDTO.setEmail(user.getEmail());
        userTaskInfoDTO.setId(user.getId());
        userTaskInfoDTO.setEmployeeName(user.getEmployeeName());
        userTaskInfoDTO.setEmployeeNumber(user.getEmployeeNumber());
        userTaskInfoDTO.setTasks(tasks);

        return userTaskInfoDTO;
    }

    @Override
    public List<UserTaskInfoDTO> findList(UserTaskInfoDTO dto) {
        return userRepository.selectUserWithTask(dto);
    }
}
