package com.hand.demo.domain.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hand.demo.infra.constant.BaseConstant;
import com.hand.demo.infra.constant.InvCountHeaderConstant;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;
import org.hzero.boot.platform.lov.annotation.LovValue;
import org.hzero.export.annotation.ExcelColumn;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * (InvoiceApplyHeader)实体类
 *
 * @author Allan
 * @since 2024-11-04 14:40:36
 */

@Getter
@Setter
@ApiModel("")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "t47359_invoice_apply_header")
public class InvoiceApplyHeader extends AuditDomain {
    private static final long serialVersionUID = -80690370382059994L;

    public static final String FIELD_APPLY_HEADER_ID = "applyHeaderId";
    public static final String FIELD_APPLY_HEADER_NUMBER = "applyHeaderNumber";
    public static final String FIELD_APPLY_STATUS = "applyStatus";
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
    public static final String FIELD_BILL_TO_ADDRESS = "billToAddress";
    public static final String FIELD_BILL_TO_EMAIL = "billToEmail";
    public static final String FIELD_BILL_TO_PERSON = "billToPerson";
    public static final String FIELD_BILL_TO_PHONE = "billToPhone";
    public static final String FIELD_DEL_FLAG = "delFlag";
    public static final String FIELD_EXCLUDE_TAX_AMOUNT = "excludeTaxAmount";
    public static final String FIELD_INVOICE_COLOR = "invoiceColor";
    public static final String FIELD_INVOICE_TYPE = "invoiceType";
    public static final String FIELD_REMARK = "remark";
    public static final String FIELD_SUBMIT_TIME = "submitTime";
    public static final String FIELD_TAX_AMOUNT = "taxAmount";
    public static final String FIELD_TENANT_ID = "tenantId";
    public static final String FIELD_TOTAL_AMOUNT = "totalAmount";

    @ApiModelProperty("PK")
    @Id
    @GeneratedValue
    @ExcelColumn(en = "apply header Id")
    private Long applyHeaderId;

    @ExcelColumn(en = "apply header number")
    private String applyHeaderNumber;

    @ApiModelProperty(value = "（need Value Set） D : Draft S : Success F : Fail C : Canceled")
    @NotEmpty
    @LovValue("DEMO-47359.INV_APPLY_HEADER.APPLY_STATUS")
    @ExcelColumn(en = "apply status")
    private String applyStatus;

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

    @ExcelColumn(en = "billing address")
    private String billToAddress;

    @ExcelColumn(en = "billing email")
    private String billToEmail;

    @ExcelColumn(en = "billing name")
    private String billToPerson;

    @ExcelColumn(en = "bill phone number")
    private String billToPhone;

    @ApiModelProperty(value = "1 : deleted 0 : normal")
    @ExcelColumn(en = "deleted")
    private Integer delFlag;

    @ApiModelProperty(value = "sum(line exclude_tax_amount)")
    @ExcelColumn(en = "exclude tax amount")
    private BigDecimal excludeTaxAmount;

    @ApiModelProperty(value = "(need Value Set) R : Red invoice B : Blue invoice")
    @NotEmpty
    @LovValue("DEMO-47359.INV_APPLY_HEADER.INV_COLOR")
    @ExcelColumn(en = "invoice color")
    private String invoiceColor;

    @ApiModelProperty(value = "(need Value Set) P : Paper invoice E : E-invoice")
    @NotEmpty
    @LovValue(value = BaseConstant.InvApplyHeader.INVOICE_TYPE_CODE, meaningField = "invoiceTypeMeaning")
    @ExcelColumn(en = "invoice type")
    private String invoiceType;

    @ExcelColumn(en = "remark")
    private String remark;

    @ExcelColumn(en = "submit time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    @ApiModelProperty(value = "sum(line tax_amount)")
    @ExcelColumn(en = "tax amount")
    private BigDecimal taxAmount;

    @ExcelColumn(en = "tenant Id")
    private Long tenantId;

    @ApiModelProperty(value = "sum(line total_amount)")
    @ExcelColumn(en = "total amount")
    private BigDecimal totalAmount;

}

