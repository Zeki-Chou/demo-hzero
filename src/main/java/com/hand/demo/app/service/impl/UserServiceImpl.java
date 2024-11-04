package com.hand.demo.app.service.impl;

import io.choerodon.mybatis.pagehelper.PageHelper;

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

import java.util.List;

import java.util.stream.Collectors;

/**
 * User Table(User)应用服务
 *
 * @author
 * @since 2024-10-31 10:28:00
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public Page<User> selectList(PageRequest pageRequest, User user) {
        return PageHelper.doPageAndSort(pageRequest, () -> userRepository.selectList(user));
    }

    @Override
    public void saveData(List<User> users) {
        List<User> insertList = users.stream().filter(line -> line.getId() == null).collect(Collectors.toList());
        List<User> updateList = users.stream().filter(line -> line.getId() != null).collect(Collectors.toList());
        userRepository.batchInsertSelective(insertList);
        userRepository.batchUpdateByPrimaryKeySelective(updateList);
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
    @Transactional(rollbackFor = Exception.class)
    public User create(Long tenantId, User user) {
//        validObject(user);
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
}

