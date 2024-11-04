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
    private final UserMapper userMapper;

    @Autowired
    public UserTaskInfoServiceImpl(UserRepository userRepository, TaskRepository taskRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.userMapper = userMapper;
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
//        List<Task> tasks = taskRepository.selectAll();
//        List<User> users = userMapper.selectUserTask();
//
//        List<UserTaskInfoDTO> userTaskInfos = new ArrayList<>();
//        for (User user: users)  {
//            List<Task> userTask = tasks
//                                    .stream()
//                                .filter(t -> t.getEmployeeId().equals(user.getId()))
//                                .collect(Collectors.toList());
//
//            if (!userTask.isEmpty()) {
//                UserTaskInfoDTO userTaskInfoDTO = new UserTaskInfoDTO();
//                BeanUtils.copyProperties(user, userTaskInfoDTO);
//                userTaskInfoDTO.setTasks(userTask);
//                userTaskInfos.add(userTaskInfoDTO);
//            }
//        }
//        return userTaskInfos;

        // your version: search all data (use database server too many resource)
        // find user only have task( select from user where exists(select
        // 1 from task where task.empid = user.id ))
        return userRepository.selectUserWithTask(dto);
    }
}
