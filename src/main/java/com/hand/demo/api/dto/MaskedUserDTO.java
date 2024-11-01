package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.User;
import io.choerodon.mybatis.util.StringUtil;

public class MaskedUserDTO extends User {
    @Override
    public String getUserPassword(){
        return "*******";
    }

    @Override
    public String getUserAccount(){
        String userAccount = super.getUserAccount();
        if(super.getUserAccount()!=null &&super.getUserAccount().length()>3){
            userAccount = super.getUserAccount().substring(0,3)+"*****";
        }
        return userAccount;
    }

    @Override
    public String getEmployeeNumber(){
        return "HAND_"+super.getEmployeeNumber();
    }
}
