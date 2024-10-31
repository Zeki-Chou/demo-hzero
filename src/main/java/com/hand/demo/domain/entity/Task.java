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
 * 任务表(Task)实体类
 *
 * @author
 * @since 2024-10-31 16:46:20
 */

@Getter
@Setter
@ApiModel("任务表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "todo_task")
public class Task extends AuditDomain {
    private static final long serialVersionUID = -27627819704297761L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_EMPLOYEE_ID = "employeeId";
    public static final String FIELD_STATE = "state";
    public static final String FIELD_TASK_DESCRIPTION = "taskDescription";
    public static final String FIELD_TASK_NUMBER = "taskNumber";
    public static final String FIELD_TASK_TYPE = "taskType";
    public static final String FIELD_TENANT_ID = "tenantId";

    @ApiModelProperty("表ID，主键，供其他表做外键")
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "员工ID，TODO_USER.ID", required = true)
    @NotNull
    private Long employeeId;

    @ApiModelProperty(value = "状态，值集：TODO.STATE", required = true)
    @NotBlank
    private String state;

    @ApiModelProperty(value = "任务描述")
    private String taskDescription;

    @ApiModelProperty(value = "任务编号", required = true)
    @NotBlank
    private String taskNumber;

    private String taskType;

    @ApiModelProperty(value = "租户ID", required = true)
    @NotNull
    private Long tenantId;


}

