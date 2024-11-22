package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.infra.constant.InvHeaderConstant;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ExcelSheet(en = "Invoice Apply Header")
@Accessors(chain = true)
public class InvoiceApplyHeaderDTO extends InvoiceApplyHeader {
    @ExcelColumn(order = 20, child = true)
    private List<InvoiceApplyLine> invoiceApplyLines;
    @Transient
    @ExcelColumn(en = InvoiceApplyHeader.FIELD_APPLY_STATUS, order = 13, lovCode = InvHeaderConstant.APPLY_STATUS_CODE)
    private String applyStatusMeaning;
    @Transient
    @ExcelColumn(en = InvoiceApplyHeader.FIELD_INVOICE_COLOR, order = 14, lovCode = InvHeaderConstant.INVOICE_COLOR_CODE)
    private String invoiceColorMeaning;
    @Transient
    @ExcelColumn(en = InvoiceApplyHeader.FIELD_INVOICE_TYPE, order = 15, lovCode = InvHeaderConstant.APPLY_TYPE_CODE)
    private String invoiceTypeMeaning;
    private Boolean superTenantAdminFlag;
    private String concatInvoiceName;
    private String rangeFirstInvoiceNumber;
    private String rangeLastInvoiceNumber;
    private Date rangeFirstInvoiceCreationDate;
    private Date rangeLastInvoiceCreationDate;
    private Date rangeFirstInvoiceSubmitTime;
    private Date rangeLastInvoiceSubmitTime;
    private List<String> listApplyStatus;
    private String userIamName;
    private String tenantName;
    private String submitTimeString;
    private String errorMsg;
}
