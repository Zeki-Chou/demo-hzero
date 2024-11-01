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
 * (OrderHeader47355)实体类
 *
 * @author
 * @since 2024-11-01 12:27:36
 */

@Getter
@Setter
@ApiModel("")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "purchase_order_header_47355")
public class OrderHeader47355 extends AuditDomain {
    private static final long serialVersionUID = 897166989113796877L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_APPROVAL_STATUS = "approvalStatus";
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
    public static final String FIELD_BUYER_ID = "buyerId";
    public static final String FIELD_ORDER_NUMBER = "orderNumber";
    public static final String FIELD_SUPPLIER_ID = "supplierId";
    public static final String FIELD_TENANT_ID = "tenantId";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "", required = true)
    @NotBlank
    private String approvalStatus;

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

    @ApiModelProperty(value = "", required = true)
    @NotNull
    private Object buyerId;

    @ApiModelProperty(value = "", required = true)
    @NotBlank
    private String orderNumber;

    @ApiModelProperty(value = "", required = true)
    @NotNull
    private Long supplierId;

    @ApiModelProperty(value = "", required = true)
    @NotNull
    private Long tenantId;


}

