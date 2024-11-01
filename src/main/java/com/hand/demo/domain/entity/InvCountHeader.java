package com.hand.demo.domain.entity;

import java.util.Date;

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
import lombok.experimental.Accessors;

/**
 * Inventory Count Header Table(InvCountHeader)实体类
 *
 * @author
 * @since 2024-10-25 13:59:59
 */

@Getter
@Setter
@ApiModel("Inventory Count Header Table")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "todo_inv_count_header")
@Accessors(chain = true)
public class InvCountHeader extends AuditDomain {
    private static final long serialVersionUID = 614044917620839261L;

    public static final String FIELD_COUNT_HEADER_ID = "countHeaderId";
    public static final String FIELD_APPROVED_TIME = "approvedTime";
    public static final String FIELD_ATTRIBUTE1 = "attribute1";
    public static final String FIELD_ATTRIBUTE10 = "attribute10";
    public static final String FIELD_ATTRIBUTE11 = "attribute11";
    public static final String FIELD_ATTRIBUTE12 = "attribute12";
    public static final String FIELD_ATTRIBUTE13 = "attribute13";
    public static final String FIELD_ATTRIBUTE14 = "attribute14";
    public static final String FIELD_ATTRIBUTE15 = "attribute15";
    public static final String FIELD_ATTRIBUTE2 = "attribute2";
    public static final String FIELD_ATTRIBUTE3 = "attribute3";
    public static final String FIELD_ATTRIBUTE4 = "attribute4";
    public static final String FIELD_ATTRIBUTE5 = "attribute5";
    public static final String FIELD_ATTRIBUTE6 = "attribute6";
    public static final String FIELD_ATTRIBUTE7 = "attribute7";
    public static final String FIELD_ATTRIBUTE8 = "attribute8";
    public static final String FIELD_ATTRIBUTE9 = "attribute9";
    public static final String FIELD_ATTRIBUTE_CATEGORY = "attributeCategory";
    public static final String FIELD_COUNT_MODE = "countMode";
    public static final String FIELD_COUNT_NUMBER = "countNumber";
    public static final String FIELD_COUNT_STATUS = "countStatus";
    public static final String FIELD_COUNT_TYPE = "countType";
    public static final String FIELD_COUNTOR_IDS = "countorIds";
    public static final String FIELD_REMARK = "remark";
    public static final String FIELD_SUPERVISOR_IDS = "supervisorIds";
    public static final String FIELD_TENANT_ID = "tenantId";
    public static final String FIELD_WORKFLOW_ID = "workflowId";

    @ApiModelProperty("Table Id")
    @Id
    @GeneratedValue
    private Long countHeaderId;

    @ApiModelProperty(value = "Approved Time")
    private Date approvedTime;

    private String attribute1;

    private String attribute10;

    private String attribute11;

    private String attribute12;

    private String attribute13;

    private String attribute14;

    private String attribute15;

    private String attribute2;

    private String attribute3;

    private String attribute4;

    private String attribute5;

    private String attribute6;

    private String attribute7;

    private String attribute8;

    private String attribute9;

    private String attributeCategory;

    @ApiModelProperty(value = "Count Mode")
    private String countMode;

    @ApiModelProperty(value = "Count Number", required = true)
    @NotBlank
    private String countNumber;

    @ApiModelProperty(value = "Count Status", required = true)
    @NotBlank
    private String countStatus;

    @ApiModelProperty(value = "Count Type")
    private String countType;

    @ApiModelProperty(value = "Counter")
    private Object countorIds;

    @ApiModelProperty(value = "Remark")
    private Object remark;

    @ApiModelProperty(value = "Supervisor")
    private Object supervisorIds;

    @ApiModelProperty(value = "Tenant Id", required = true)
    @NotNull
    private Long tenantId;

    @ApiModelProperty(value = "Workflow Id")
    private Long workflowId;


}

