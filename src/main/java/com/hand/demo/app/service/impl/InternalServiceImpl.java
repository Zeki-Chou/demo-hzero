package com.hand.demo.app.service.impl;

import com.hand.demo.api.controller.dto.InternalUserDTO;
import com.hand.demo.app.service.InternalService;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.UserRepository;
import com.hand.demo.infra.util.Utils;
import io.choerodon.core.exception.CommonException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InternalServiceImpl implements InternalService {

    private final UserRepository userRepository;

    public InternalServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<InternalUserDTO> saveUser(List<InternalUserDTO> users) {

        List<User> userList = new ArrayList<>();
        User newEmployee = new User();

        users.forEach(user -> {

            if (user == null) {
                throw new CommonException("user value null");
            }

            newEmployee.setEmployeeName(user.getEmployee());
            newEmployee.setEmployeeNumber(user.getEmployeeNo());
            newEmployee.setEmail(user.getEmail());
            newEmployee.setUserAccount(user.getAccount());
            newEmployee.setUserPassword(user.getPassword());
            userList.add(newEmployee);
        });

        List<User> recentlyRegisteredUsers = userRepository.batchInsertSelective(userList);

        StringBuilder stringBuilder = new StringBuilder();

        return recentlyRegisteredUsers.stream().map(user -> {

            InternalUserDTO internalUserDTO = new InternalUserDTO();

            internalUserDTO.setEmail(user.getEmail());
            internalUserDTO.setEmployee(user.getEmployeeName());
            internalUserDTO.setEmployeeNo("HAND_" + user.getEmployeeNumber());

            int lengthForAccountMask = user.getUserAccount().length() - 3;

            if (lengthForAccountMask < 1) {
                throw new CommonException("account length must at least be 1");
            }

            stringBuilder.append(user.getUserAccount());
            stringBuilder.replace(3, stringBuilder.length(), Utils.generateNStringMasking(lengthForAccountMask, "*"));
            String maskedAccount = stringBuilder.toString();
            internalUserDTO.setAccount(maskedAccount);
            stringBuilder.delete(0, stringBuilder.length());

            stringBuilder.append(user.getUserPassword());
            if (stringBuilder.length() > 0) {
                stringBuilder.replace(0, stringBuilder.length(), Utils.generateNStringMasking(user.getUserPassword().length(), "*"));
                internalUserDTO.setPassword(stringBuilder.toString());
            }

            stringBuilder.delete(0, stringBuilder.length());

            return internalUserDTO;

        }).collect(Collectors.toList());

    }
}
