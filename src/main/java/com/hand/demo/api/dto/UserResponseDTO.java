package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.User;
import io.choerodon.mybatis.util.StringUtil;
import lombok.Getter;
import org.dom4j.util.StringUtils;

import javax.validation.constraints.NotBlank;

public class UserResponseDTO extends User {

    @Override
    public String getEmployeeNumber() {
        String number = super.getEmployeeNumber();
        return "HAND_" + number;
    }

    @Override
    public String getUserAccount() {
        String account = super.getUserAccount();
        if (account != null && account.length() > 3) {
            return account.substring(0, 3) + "****";
        }
        return account;
    }

    @Override
    public String getUserPassword() {
        String password = super.getUserPassword();
        if (password != null) {
            StringBuilder maskedPassword = new StringBuilder();
            for (int i = 0; i < password.length(); i++) {
                maskedPassword.append('*');
            }
            return maskedPassword.toString();
        }
        return "********";
    }
}
