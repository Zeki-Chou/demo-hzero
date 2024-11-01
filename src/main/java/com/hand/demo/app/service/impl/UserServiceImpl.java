package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.UserTasksDTO;
import com.hand.demo.api.dto.UserTasksRequest;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.repository.TaskRepository;
import com.hand.demo.infra.util.Utils;
import io.choerodon.core.exception.ext.IllegalArgumentException;
import org.hzero.core.base.BaseAppService;

import org.hzero.mybatis.domian.Condition;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import com.hand.demo.app.service.UserService;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户表应用服务默认实现
 *
 * @author joseph.julio@hand-global.com 2024-10-17 13:56:56
 */
@Service
public class UserServiceImpl extends BaseAppService implements UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public Page<User> list(Long tenantId, User user, PageRequest pageRequest) {
        return userRepository.pageAndSort(pageRequest, user);
    }

    public List<UserTasksDTO> getUsersWithTasks(UserTasksDTO userTasksDTO) {
        return userRepository.findUsersWithTasks(userTasksDTO.getEmployeeNumber(), userTasksDTO.getTaskNumber());
    }

    @Override
    public List<UserTasksDTO> getUserWithTasks(Long tenantId, User user, PageRequest pageRequest) {
        // your version : query all data -> filter(use more database source)
        // query what you want(emp have task) data
        Page<User> users = userRepository.pageAndSort(pageRequest, user);

        List<Long> userIds = users.getContent().stream()
                .map(User::getId)
                .collect(Collectors.toList());

        if(userIds.isEmpty()) {
            return Collections.emptyList();
        }
        Condition condition = new Condition(Task.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andIn("employeeId", userIds);
        List<Task> tasks = taskRepository.selectByCondition(condition);

        Map<Long, List<Task>> tasksByUserId = tasks.stream()
                .collect(Collectors.groupingBy(Task::getEmployeeId));

        List<UserTasksDTO> userTasksDTOS = users.getContent().stream()
                .map(u -> {
                    List<Task> userTasks = tasksByUserId.getOrDefault(u.getId(), Collections.emptyList());

                    if (!userTasks.isEmpty()) {
                        UserTasksDTO userTasksDTO = new UserTasksDTO();
                        userTasksDTO.setId(u.getId());
                        userTasksDTO.setEmail(u.getEmail());
                        userTasksDTO.setEmployeeName(u.getEmployeeName());
                        userTasksDTO.setEmployeeNumber(u.getEmployeeNumber());
                        userTasksDTO.setTasks(userTasks);
                        return userTasksDTO;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return userTasksDTOS;
    }

    @Override
    public User detail(Long tenantId, Long id) {
        return userRepository.selectByPrimaryKey(id);
    }

    @Override
    public User create(Long tenantId, User user) {
        validObject(user);
        userRepository.insertSelective(user);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User update(Long tenantId, User user) {
        SecurityTokenHelper.validToken(user);
        userRepository.updateByPrimaryKeySelective(user);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(User user) {
        SecurityTokenHelper.validToken(user);
        userRepository.deleteByPrimaryKey(user);
    }

    @Override
    public List<User> saveData(List<User> users) {
        List<User> insertList = users.stream().filter(line -> line.getId() == null).collect(Collectors.toList());
        List<User> updateList = users.stream().filter(line -> line.getId() != null).collect(Collectors.toList());
        List<User> mappedUsers = users.stream().peek(user -> {
            if(user.getUserAccount() == null || user.getUserAccount().isEmpty()) {
                throw new IllegalArgumentException("User account can't be empty");
            }
            user.setUserPassword(Utils.desensitizeString(user.getUserPassword()))
                            .setUserAccount(Utils.desensitizeString(user.getUserAccount(), 3))
                            .setEmployeeNumber("HAND_" + user.getEmployeeNumber());
                })
                .collect(Collectors.toList());

        userRepository.batchInsertSelective(insertList);
        userRepository.batchUpdateByPrimaryKeySelective(updateList);
        return mappedUsers;
    }
}
