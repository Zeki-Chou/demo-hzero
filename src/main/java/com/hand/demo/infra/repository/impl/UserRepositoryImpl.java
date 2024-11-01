package com.hand.demo.infra.repository.impl;

import com.hand.demo.api.dto.UserTasksDTO;
import com.hand.demo.infra.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.UserRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户表 资源库实现
 *
 * @author joseph.julio@hand-global.com 2024-10-17 13:56:56
 */
@Component
@AllArgsConstructor
public class UserRepositoryImpl extends BaseRepositoryImpl<User> implements UserRepository {
    private UserMapper userMapper;

    public List<UserTasksDTO> findUsersWithTasks(
            String employeeNumber,
            String taskNumber
    ) {
        return userMapper.findUsersWithTasks(employeeNumber, taskNumber);
    }

    @Override
    public List<User> selectList(User user) {
        return userMapper.selectList(user);
    }

    @Override
    public User selectByPrimary(Long id) {
        User user = new User();
        user.setId(id);
        List<User> users = userMapper.selectList(user);
        if (users.size() == 0) {
            return null;
        }
        return users.get(0);
    }
}
