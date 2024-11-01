package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.dto.DesensitizedUserDTO;
import com.hand.demo.domain.dto.ExternalInterfaceDTO;
import com.hand.demo.domain.dto.UserDTO;
import com.hand.demo.domain.dto.UserTaskDTO;
import com.hand.demo.infra.mapper.UserMapper;
import org.hzero.boot.interfaces.sdk.dto.RequestPayloadDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;
import org.hzero.boot.interfaces.sdk.invoke.InterfaceInvokeSdk;
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
 * @author azhar.naufal@hand-global.com 2024-10-17 13:48:26
 */
@Service
public class UserServiceImpl extends BaseAppService implements UserService {

    @Autowired
    private InterfaceInvokeSdk interfaceInvokeSdk;

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

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

    @Override
    public void saveData(List<User> users) {
        List<User> insertList = users.stream().filter(line -> line.getId() == null).collect(Collectors.toList());
        List<User> updateList = users.stream().filter(line -> line.getId() != null).collect(Collectors.toList());
        userRepository.batchInsertSelective(insertList);
        userRepository.batchUpdateByPrimaryKeySelective(updateList);
    }

    @Override
    public Page<UserTaskDTO> usersWithTask(Long tenantId, PageRequest pageRequest) {
        // query once databse
        // write your own sql  user join task result user+task info
        List<UserTaskDTO> userTaskDTOList = userMapper.selectUsersWithTasks();

        Page<UserTaskDTO> resultPage = new Page<>();
        resultPage.setContent(userTaskDTOList); // Set the content
        resultPage.setTotalElements(userTaskDTOList.size()); // Set total elements
        resultPage.setTotalPages(pageRequest.getPage()); // Set the current page number
        resultPage.setSize(pageRequest.getSize());

        return resultPage;
    }

    @Override
    public List<DesensitizedUserDTO> saveDataMasking(List<User> users) {

        List<User> insertList = users.stream().filter(line -> line.getId() == null).collect(Collectors.toList());
        List<User> updateList = users.stream().filter(line -> line.getId() != null).collect(Collectors.toList());


        userRepository.batchInsertSelective(insertList);
        userRepository.batchUpdateByPrimaryKeySelective(updateList);


        List<DesensitizedUserDTO> desensitizedUsers = users.stream()
                .map(user ->
                        new DesensitizedUserDTO(
                        user.getEmployeeName(),
                        "HAND_" + user.getEmployeeNumber(),
                        user.getEmail(),
                        user.getUserAccount() != null && user.getUserAccount().length() > 3
                                ? user.getUserAccount().substring(0, 3) + "****"
                                : user.getUserAccount(),
                        maskPassword(user.getUserPassword())))
                .collect(Collectors.toList());

        return desensitizedUsers;
    }


    @Override
    public List<UserTaskDTO> userWithTask(Long tenantId, UserTaskDTO userTaskDTO){
        String employeeNumber = userTaskDTO.getEmployeeNumber();
        String taskNumber = userTaskDTO.getTaskNumber();
        List<UserTaskDTO> userTaskDTOList = userRepository.selectUserTaskByEmpOrTaskNumber(employeeNumber, taskNumber);
        return userTaskDTOList;
    }

    @Override
    public ResponsePayloadDTO invokeSaveUSer(ExternalInterfaceDTO externalInterfaceDTO, List<UserDTO> listUserDTO){
        ObjectMapper objectMapper = new ObjectMapper();
        String listJsonUser = "";

        try {
            listJsonUser = objectMapper.writeValueAsString(listUserDTO);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        RequestPayloadDTO requestPayloadDTO = new RequestPayloadDTO();
        requestPayloadDTO.setPayload(listJsonUser);
        requestPayloadDTO.setMediaType("application/json");

        ResponsePayloadDTO response = interfaceInvokeSdk.invoke(externalInterfaceDTO.getNamespace(), externalInterfaceDTO.getServerCode(), externalInterfaceDTO.getInterfaceCode(), requestPayloadDTO);

        return response;
    }




    private String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return ""; // Return empty if no password
        }

        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            masked.append('*'); // Append an asterisk for each character in the password
        }
        return masked.toString(); // Convert StringBuilder to String
    }
}
