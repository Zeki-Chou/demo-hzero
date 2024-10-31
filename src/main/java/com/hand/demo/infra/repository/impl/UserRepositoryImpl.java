package com.hand.demo.infra.repository.impl;

import org.apache.commons.collections.CollectionUtils;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.UserRepository;
import com.hand.demo.infra.mapper.UserMapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * User Table(User)资源库
 *
 * @author
 * @since 2024-10-31 16:45:12
 */
@Component
public class UserRepositoryImpl extends BaseRepositoryImpl<User> implements UserRepository {
    @Resource
    private UserMapper userMapper;

    @Override
    public List<User> selectList(User user) {
        return userMapper.selectList(user);
    }

    @Override
    public User selectByPrimary(Long $pk.name) {
        User user = new User();
        user.set$tool.firstUpperCase($pk.name) ($pk.name);
        List<User> users = userMapper.selectList(user);
        if (users.size() == 0) {
            return null;
        }
        return users.get(0);
    }

}

