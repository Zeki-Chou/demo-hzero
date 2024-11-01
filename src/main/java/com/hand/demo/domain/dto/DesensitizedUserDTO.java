package com.hand.demo.domain.dto;

import com.hand.demo.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DesensitizedUserDTO extends User {
    private String employeeName;
    private String employeeNumber;
    private String email;
    private String userAccount;
    private String userPassword;

}
