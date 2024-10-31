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
 * @since 2024-10-31 16:44:57
 */

@Getter
@Setter
@ApiModel("任务表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "todo_task")
public class Task extends AuditDomain {
    private static final long serialVersionUID = 617848079749916438L;


}

