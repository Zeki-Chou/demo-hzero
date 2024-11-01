package com.hand.demo.app.service.impl;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户表应用服务默认实现
 *
 * @author muhammad.azzam@hand-global.com 2024-10-17 13:53:42
 */
@Service
public class UserServiceImpl extends BaseAppService implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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
    public Page<User> selectList(PageRequest pageRequest, User user) {
        return PageHelper.doPageAndSort(pageRequest, () -> userRepository.selectList(user));
    }

    @Override
    public void saveData(List<User> users, List<User> responseUsers) {

        users.forEach(user -> {

            if (user.getEmployeeNumber() != null) {
                user.setEmployeeNumber("HAND_" + user.getEmployeeNumber());
            }

            String actualUserAccount = user.getUserAccount();
            String maskedUserAccount = "";

            if (actualUserAccount != null) {
                if (actualUserAccount.length() <= 3) {

                    StringBuilder maskedBuilder = new StringBuilder();
                    for (int i = 0; i < actualUserAccount.length(); i++) {
                        maskedBuilder.append("*");
                    }
                    maskedUserAccount = maskedBuilder.toString();
                } else {

                    StringBuilder maskedBuilder = new StringBuilder(actualUserAccount.substring(0, 3));
                    for (int i = 3; i < actualUserAccount.length(); i++) {
                        maskedBuilder.append("*");
                    }
                    maskedUserAccount = maskedBuilder.toString();
                }
            }

            String actualPassword = user.getUserPassword();
            String maskedPassword ="";

            if (actualPassword != null) {
                StringBuilder maskedBuilder = new StringBuilder();
                for (int i = 0; i < actualPassword.length(); i++) {
                    maskedBuilder.append("*");
                }
                maskedPassword = maskedBuilder.toString();
            }

            User responseUser = new User();
            responseUser.setEmployeeNumber(user.getEmployeeNumber());
            responseUser.setUserAccount(maskedUserAccount);
            responseUser.setEmployeeName(user.getEmployeeName());
            responseUser.setEmail(user.getEmail());
            responseUser.setUserPassword(maskedPassword);
            responseUsers.add(responseUser);
        });

        List<User> insertList = users.stream().filter(user -> user.getId() == null).collect(Collectors.toList());
        List<User> updateList = users.stream().filter(user -> user.getId() != null).collect(Collectors.toList());

        userRepository.batchInsertSelective(insertList);
        userRepository.batchUpdateByPrimaryKeySelective(updateList);
    }

}
