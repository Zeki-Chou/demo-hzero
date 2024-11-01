package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.UserDTO;
import com.hand.demo.api.dto.UserResponseDTO;
import com.hand.demo.domain.repository.TaskRepository;
import com.hand.demo.infra.mapper.TaskMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import org.hzero.core.base.BaseAppService;

import org.hzero.mybatis.helper.SecurityTokenHelper;
import com.hand.demo.app.service.UserService;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户表应用服务默认实现
 *
 * @author fatih.khoiri@hand-global.com 2024-10-17 13:57:07
 */
@Service
public class UserServiceImpl extends BaseAppService implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

//    @Autowired
//    public UserServiceImpl(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }

    @Override
    public Page<User> list(Long tenantId, User user, PageRequest pageRequest) {
        return userRepository.pageAndSort(pageRequest, user);
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

//    @Override
//    public List<UserDTO> listTask(Long tenantId, UserDTO userDTO) {
//        List<Task> tasks;
//        List<UserDTO> userDTOs = new ArrayList<>();
//        User user1;
//
//        if (userDTO.getEmployeeNumber() != null) {
//            user1 = userRepository.selectOne(userDTO);
//            if (user1 != null) {
//                UserDTO dto = new UserDTO();
//                if (userDTO.getTaskNumber() != null) {
//                    Condition condition = new Condition(Task.class);
//                    Condition.Criteria criteria = condition.createCriteria();
//                    criteria.andEqualTo("taskNumber",userDTO.getTaskNumber());
//                    tasks = taskRepository.selectByCondition(condition);
//
//                    Long userId = tasks.get(0).getEmployeeId();
//                    User userGet = userRepository.selectByPrimaryKey(userId);
//
//                    dto.setId(userGet.getId());
//                    dto.setEmployeeName(userGet.getEmployeeName());
//                    dto.setEmployeeNumber(userGet.getEmployeeNumber());
//                    dto.setEmail(userGet.getEmail());
//                    dto.setTaskList(tasks);
//                    userDTOs.add(dto);
//                } else {
//                    tasks = taskMapper.selectTasksByEmployeeId(user1.getId());
//                    BeanUtils.copyProperties(user1, dto);
////                dto.setId(user1.getId());
////                dto.setEmployeeName(user1.getEmployeeName());
////                dto.setEmployeeNumber(user1.getEmployeeNumber());
////                dto.setEmail(user1.getEmail());
//                    dto.setTaskList(tasks);
//                    userDTOs.add(dto);
//                }
//            }
//            return userDTOs;
//        } else if (userDTO.getTaskNumber() != null) {
//            Condition condition = new Condition(Task.class);
//            Condition.Criteria criteria = condition.createCriteria();
//            criteria.andEqualTo("taskNumber",userDTO.getTaskNumber());
//            tasks = taskRepository.selectByCondition(condition);
//
//            if (tasks != null) {
//                Long userId = tasks.get(0).getEmployeeId();
//                User userGet = userRepository.selectByPrimaryKey(userId);
//                UserDTO dto = new UserDTO();
//                dto.setId(userGet.getId());
//                dto.setEmployeeName(userGet.getEmployeeName());
//                dto.setEmployeeNumber(userGet.getEmployeeNumber());
//                dto.setEmail(userGet.getEmail());
//                dto.setTaskList(tasks);
//                userDTOs.add(dto);
//            }
//        } else {
//            List<User> userList = userRepository.selectAll();
//
//            tasks = taskMapper.selectTasksWhereEmployeeIdNotNull();
//
//            userDTOs = userList.stream()
//                    .map(u -> {
//                        UserDTO dto = new UserDTO();
//                        dto.setId(u.getId());
//                        dto.setEmployeeName(u.getEmployeeName());
//                        dto.setEmployeeNumber(u.getEmployeeNumber());
//                        dto.setEmail(u.getEmail());
//                        dto.setTaskList(tasks);
//                        return dto;
//                    })
//                    .collect(Collectors.toList());
//        }
//        return userDTOs;
//    }


//    @Override
//    public List<UserDTO> listTask(Long tenantId, User user, Task taskParam) {
//        List<Long> userIds;
//        List<Task> tasks;
//        List<UserDTO> userDTOs = new ArrayList<>();
//        User userGet;
//        Page<User> userPage = userRepository.pageAndSort(pageRequest, user);
//        List<Long> userIds = userPage.getContent().stream()
//                .map(User::getId)
//                .collect(Collectors.toList());
//
//        List<Task> tasks = taskMapper.selectTasksByEmployeeIds(userIds);
//
//        List<UserDTO> userDTOs = userPage.getContent().stream()
//                .map(u -> {
//                    UserDTO dto = new UserDTO();
//                    dto.setId(u.getId());
//                    dto.setEmployeeName(u.getEmployeeName());
//                    dto.setEmployeeNumber(u.getEmployeeNumber());
//                    dto.setEmail(u.getEmail());
//
//                    List<Task> userTasks = tasks.stream()
//                            .filter(task -> task.getEmployeeId().equals(u.getId()))
//                            .collect(Collectors.toList());
//                    dto.setTaskList(userTasks);
//                    return dto;
//                })
//                .collect(Collectors.toList());
//
//        return userDTOs;

//        ---- SUCCESS -----

//        if (taskParam.getTaskNumber() != null) {
//            tasks = taskRepository.select(taskParam);
//
//            if (!tasks.isEmpty()) {
//                Long userId = tasks.get(0).getEmployeeId();
//
//                userGet = userRepository.selectByPrimaryKey(userId);
//                UserDTO dto = new UserDTO();
//                dto.setId(userGet.getId());
//                dto.setEmployeeName(userGet.getEmployeeName());
//                dto.setEmployeeNumber(userGet.getEmployeeNumber());
//                dto.setEmail(userGet.getEmail());
//                dto.setTaskList(tasks);
//                userDTOs.add(dto);
//            }
//
//        } else if (user.getEmployeeNumber() != null) {
//            User user1 = userRepository.select(user).stream().findFirst().orElse(null);
//
//            if (user1 != null) {
//                tasks = taskMapper.selectTasksByEmployeeId(user1.getId());
//                UserDTO dto = new UserDTO();
//                dto.setId(user1.getId());
//                dto.setEmployeeName(user1.getEmployeeName());
//                dto.setEmployeeNumber(user1.getEmployeeNumber());
//                dto.setEmail(user1.getEmail());
//                dto.setTaskList(tasks);
//
//                userDTOs.add(dto);
//            }
//
//            return userDTOs;
//        } else {
//            List<User> userList = userRepository.selectAll();
//            userIds = userList.stream()
//                    .map(User::getId)
//                    .collect(Collectors.toList());
//
//            tasks = taskMapper.selectTasksByEmployeeIds(userIds);
//
//            userDTOs = userList.stream()
//                    .map(u -> {
//                        UserDTO dto = new UserDTO();
//                        dto.setId(u.getId());
//                        dto.setEmployeeName(u.getEmployeeName());
//                        dto.setEmployeeNumber(u.getEmployeeNumber());
//                        dto.setEmail(u.getEmail());
//
//                        List<Task> userTasks = tasks.stream()
//                                .filter(task -> task.getEmployeeId().equals(u.getId()))
//                                .collect(Collectors.toList());
//                        dto.setTaskList(userTasks);
//                        return dto;
//                    })
//                    .collect(Collectors.toList());
//        }
//
//        return userDTOs;
//    }


    @Override
    public List<UserDTO> listTask(Long tenantId, UserDTO userDTO) {
        return userRepository.findUserTask(userDTO);
    }

    @Override
    public Page<User> selectList(PageRequest pageRequest, User user) {
        return PageHelper.doPageAndSort(pageRequest, () -> userRepository.selectList(user));
    }

    @Override
    public List<UserResponseDTO> saveData(List<User> users) {
        List<User> insertList = users.stream().filter(line -> line.getId() == null).collect(Collectors.toList());
        List<User> updateList = users.stream().filter(line -> line.getId() != null).collect(Collectors.toList());
        userRepository.batchInsertSelective(insertList);
        userRepository.batchUpdateByPrimaryKeySelective(updateList);
        List<UserResponseDTO> userResponseDTOS = insertList.stream()
                .map(user -> {
                    UserResponseDTO dto = new UserResponseDTO();
                    dto.setId(user.getId());
                    dto.setEmail(user.getEmail());
                    dto.setEmployeeName(user.getEmployeeName());
                    dto.setEmployeeNumber(user.getEmployeeNumber());
                    dto.setUserAccount(user.getUserAccount());
                    dto.setUserPassword(user.getUserPassword());
                    return dto;
                })
                .collect(Collectors.toList());

        return userResponseDTOS;
    }
}
