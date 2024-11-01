package com.hand.demo.infra.repository.impl;

import com.hand.demo.api.dto.UserDTO;
import com.hand.demo.infra.mapper.TaskMapper;
import com.hand.demo.infra.mapper.UserMapper;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 用户表 资源库实现
 *
 * @author fatih.khoiri@hand-global.com 2024-10-17 13:57:07
 */
@Component
public class UserRepositoryImpl extends BaseRepositoryImpl<User> implements UserRepository {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<UserDTO> findUserTask(UserDTO userDTO) {
        List<UserDTO> userDTOS = taskMapper.selectUserTasks(userDTO);
        return userDTOS;
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
