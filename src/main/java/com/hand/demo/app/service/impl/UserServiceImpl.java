package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.MaskedUserDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.UserService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User Table(User)应用服务
 *
 * @author
 * @since 2024-10-28 17:04:00
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
    public List<MaskedUserDTO> saveData(List<User> users) {
        List<User> insertList = users.stream().filter(line -> line.getId() == null).collect(Collectors.toList());
        List<User> updateList = users.stream().filter(line -> line.getId() != null).collect(Collectors.toList());
        userRepository.batchInsertSelective(insertList);
        userRepository.batchUpdateByPrimaryKeySelective(updateList);

        List<MaskedUserDTO> maskedUserDTOS = new ArrayList<>();
        users.forEach(user -> {
            MaskedUserDTO maskedUserDTO = new MaskedUserDTO();
            BeanUtils.copyProperties(user,maskedUserDTO);
            maskedUserDTOS.add(maskedUserDTO);
        });

        return maskedUserDTOS;
    }
}

