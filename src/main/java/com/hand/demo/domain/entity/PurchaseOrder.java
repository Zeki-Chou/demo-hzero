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

/**
 * (PurchaseOrder)实体类
 *
 * @author
 * @since 2024-11-01 10:50:58
 */

@Getter
@Setter
@ApiModel("")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "purchase_order_47359")
public class PurchaseOrder extends AuditDomain {
    private static final long serialVersionUID = 706626372133444317L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_APPROVED_TIME = "approvedTime";
    public static final String FIELD_ATTRIBUTE15 = "attribute15";
    public static final String FIELD_ATTRIBUTE_CATEGORY = "attributeCategory";
    public static final String FIELD_BUYER_IDS = "buyerIds";
    public static final String FIELD_COUNT_STATUS = "countStatus";
    public static final String FIELD_PURCHASE_ORDER_NUMBER = "purchaseOrderNumber";
    public static final String FIELD_SUPPLIER_ID = "supplierId";
    public static final String FIELD_TENANT_ID = "tenantId";
    public static final String FIELD_WORKFLOW_ID = "workflowId";

    @ApiModelProperty("purchase id")
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "Approved Time")
    private Date approvedTime;

    private String attribute15;

    private String attributeCategory;

    @ApiModelProperty(value = "", required = true)
    @NotBlank
    private String buyerIds;

    @ApiModelProperty(value = "Count Status", required = true)
    @NotBlank
    private String countStatus;

    @ApiModelProperty(value = "Count Number", required = true)
    @NotBlank
    private String purchaseOrderNumber;

    @ApiModelProperty(value = "", required = true)
    @NotNull
    private Long supplierId;

    @ApiModelProperty(value = "", required = true)
    @NotNull
    private Long tenantId;

    private Long workflowId;


}

