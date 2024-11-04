package com.hand.demo.infra.repository.impl;

import com.hand.demo.api.controller.dto.UserTaskInfoDTO;
import com.hand.demo.infra.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户表 资源库实现
 *
 * @author allan.sugianto@hand-global.com 2024-10-17 14:34:10
 */
@Component
@AllArgsConstructor
public class UserRepositoryImpl extends BaseRepositoryImpl<User> implements UserRepository {
    private UserMapper userMapper;

    @Override
    public List<User> selectList(User user) {
        return userMapper.selectList(user);
    }

    @Override
    public User selectByPrimary(Long id) {
        return userMapper.selectByPrimaryKey(id);
    }

    public List<UserTaskInfoDTO> selectUserWithTask(UserTaskInfoDTO dto) {
        return userMapper.selectUserWithTask(dto);
    }
}
