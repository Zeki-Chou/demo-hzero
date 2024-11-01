package com.hand.demo.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * User Table(User)实体类
 *
 * @author
 * @since 2024-10-31 09:26:48
 */

@Getter
@Setter
@ApiModel("User Table")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "todo_user")
public class User extends AuditDomain {
    private static final long serialVersionUID = -60214179786959595L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_EMPLOYEE_NAME = "employeeName";
    public static final String FIELD_EMPLOYEE_NUMBER = "employeeNumber";
    public static final String FIELD_USER_ACCOUNT = "userAccount";
    public static final String FIELD_USER_PASSWORD = "userPassword";

    @ApiModelProperty("Table Id")
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "Email")
    private String email;

    @ApiModelProperty(value = "Employee Name", required = true)
    @NotBlank
    private String employeeName;

    @ApiModelProperty(value = "Employee Number", required = true)
    @NotBlank
    private String employeeNumber;

    @ApiModelProperty(value = "User Account")
    private String userAccount;

    @ApiModelProperty(value = "User Password")
    private String userPassword;


}

