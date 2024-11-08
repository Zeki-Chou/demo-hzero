package com.hand.demo.domain.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import org.hzero.boot.platform.lov.annotation.LovValue;
import org.hzero.export.annotation.ExcelColumn;

/**
 * (InvoiceApplyHeader)实体类
 *
 * @author
 * @since 2024-11-04 10:16:07
 */

@Getter
@Setter
@ApiModel("")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "T47356_invoice_apply_header")
public class InvoiceApplyHeader extends AuditDomain {
    private static final long serialVersionUID = 211828222303523875L;

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

    @ExcelColumn(en = "apply_header_id")
    @Column(name = "apply_header_id")
    @Id
    @GeneratedValue
    private Long applyHeaderId;

    @ExcelColumn(en = "apply_header_number")
    @Column(name = "apply_header_number")
    private String applyHeaderNumber;

    @ExcelColumn(en = "apply_status")
    @Column(name = "apply_status")
    @NotEmpty
    @LovValue(lovCode = "DEMO-47356.INV_APPLY_HEADER.APPLY_STATUS")
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

    @ExcelColumn(en = "bill_to_address")
    @Column(name = "bill_to_address")
    private String billToAddress;

    @ExcelColumn(en = "bill_to_email")
    @Column(name = "bill_to_email")
    private String billToEmail;

    @ExcelColumn(en = "bill_to_person")
    @Column(name = "bill_to_person")
    private String billToPerson;

    @ExcelColumn(en = "bill_to_phone")
    @Column(name = "bill_to_phone")
    private String billToPhone;

    @ExcelColumn(en = "del_flag")
    @Column(name = "del_flag")
    private Integer delFlag;

    @ExcelColumn(en = "exclude_tax_amount")
    @Column(name = "exclude_tax_amount")
    private BigDecimal excludeTaxAmount;

    @ExcelColumn(en = "invoice_color")
    @Column(name = "invoice_color")
    @NotEmpty
    @LovValue(lovCode = "DEMO-47356.INV_APPLY_HEADER.INV_COLOR")
    private String invoiceColor;

    @ExcelColumn(en = "invoice_type")
    @Column(name = "invoice_type")
    @NotEmpty
    @LovValue(lovCode = "DEMO-47356.INV_APPLY_HEADER.INV_TYPE")
    private String invoiceType;

    @ExcelColumn(en = "remark")
    @Column(name = "remark")
    private String remark;

    @ExcelColumn(en = "submit_time")
    @Column(name = "submit_time")
    private Date submitTime;

    @ExcelColumn(en = "tax_amount")
    @Column(name = "tax_amount")
    private BigDecimal taxAmount;

    @ExcelColumn(en = "tenant_id")
    @Column(name = "tenant_id")
    private Long tenantId;

    @ExcelColumn(en = "total_amount")
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
}

