package com.hand.demo.app.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.UserService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User Table(User)应用服务
 *
 * @author
 * @since 2024-10-31 16:45:13
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
        List<User> insertList = users.stream().filter(line -> line.get$tool.firstUpperCase($ {
            pk.name
        })() == null).collect(Collectors.toList());
        List<User> updateList = users.stream().filter(line -> line.get$tool.firstUpperCase($ {
            pk.name
        })() != null).collect(Collectors.toList());
        userRepository.batchInsertSelective(insertList);
        userRepository.batchUpdateByPrimaryKeySelective(updateList);
    }
}

