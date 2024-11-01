package com.hand.demo.infra.repository.impl;

import com.hand.demo.domain.dto.UserTaskDTO;
import com.hand.demo.infra.mapper.UserMapper;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户表 资源库实现
 *
 * @author azhar.naufal@hand-global.com 2024-10-17 13:48:26
 */
@Component
public class UserRepositoryImpl extends BaseRepositoryImpl<User> implements UserRepository {
    private final UserMapper userMapper;

    public UserRepositoryImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public List<UserTaskDTO> selectUserTaskByEmpOrTaskNumber(String employeeNumber, String taskNumber){
        return userMapper.selectUserTaskByEmpOrTaskNumber(employeeNumber, taskNumber);
    }
}
