package com.hand.demo.domain.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hand.demo.infra.constant.InvoiceApplyConstants;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import org.hzero.boot.platform.lov.annotation.LovValue;
import org.hzero.export.annotation.ExcelColumn;

/**
 * (InvoiceApplyHeader)实体类
 *
 * @author
 * @since 2024-11-04 11:43:29
 */

@Getter
@Setter
@ApiModel("")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "47355_invoice_apply_header")
public class InvoiceApplyHeader extends AuditDomain {
    private static final long serialVersionUID = -27419393397786132L;

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
    @ExcelColumn(en = "Apply Header ID", order = 1)
    private Long applyHeaderId;

    @ExcelColumn(en = "Apply Header Number", order = 2)
    private String applyHeaderNumber;

    @NotEmpty
    @LovValue(lovCode = InvoiceApplyConstants.INV_APPLY_HEADER_APPLY_STATUS)
    @ApiModelProperty(value = "（need Value Set） D : Draft S : Success F : Fail C : Canceled")
    @ExcelColumn(en = "Apply Status", order = 3)
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

    @ExcelColumn(en = "Bill to Address", order = 4)
    private String billToAddress;

    @ExcelColumn(en = "Bill to Email", order = 5)
    private String billToEmail;

    @ExcelColumn(en = "Bill to Person", order = 6)
    private String billToPerson;

    @ExcelColumn(en = "Bill to Phone", order = 7)
    private String billToPhone;

    @ApiModelProperty(value = "1 : deleted 0 : normal")
    @ExcelColumn(en = "Del Flag", order = 8)
    private Integer delFlag;

    @ApiModelProperty(value = "sum(line exclude_tax_amount)")
    @ExcelColumn(en = "Exclude Tax Amount", order = 9)
    private BigDecimal excludeTaxAmount;

    @NotEmpty
    @LovValue(lovCode = InvoiceApplyConstants.INV_APPLY_HEADER_INV_COLOR)
    @ApiModelProperty(value = "(need Value Set) R : Red invoice B : Blue invoice")
    @ExcelColumn(en = "Invoice Color", order = 10)
    private String invoiceColor;

    @NotEmpty
    @LovValue(lovCode = InvoiceApplyConstants.INV_APPLY_HEADER_INV_TYPE)
    @ApiModelProperty(value = "(need Value Set) P : Paper invoice E : E-invoice")
    @ExcelColumn(en = "Invoice Type", order = 11)
    private String invoiceType;

    @ExcelColumn(en = "Remark", order = 12)
    private Object remark;

    @ExcelColumn(en = "Submit Time", order = 13)
    private Date submitTime;

    @ApiModelProperty(value = "sum(line tax_amount)")
    @ExcelColumn(en = "Tax Amount", order = 14)
    private BigDecimal taxAmount;

    @ExcelColumn(en = "Tenant ID", order = 15)
    private Long tenantId;

    @ApiModelProperty(value = "sum(line total_amount)")
    @ExcelColumn(en = "Total Amount", order = 16)
    private BigDecimal totalAmount;

}

