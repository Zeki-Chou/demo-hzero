package com.hand.demo.domain.dto;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ExcelSheet(en = "Invoice Apply Header")
public class InvoiceApplyHeaderDTO extends InvoiceApplyHeader {
    @ExcelColumn(en = "Apply Status Meaning", order = 17, lovCode = "DEMO-47361.INV_HEADER.APPLY_STATUS")
    private String applyStatusMeaning;

    @ExcelColumn(en = "Invoice Color Meaning", order = 18, lovCode = "DEMO-47361.INV_HEADER.INVOICE_COLOR")
    private String invoiceColorMeaning;

    @ExcelColumn(en = "Invoice Type", order = 19, lovCode = "DEMO-47361.INV_HEADER.INVOICE_TYPE")
    private String invoiceTypeMeaning;

    @ExcelColumn(en = "List Invoice Line", order = 20, child = true)
    private List<InvoiceApplyLine> invoiceApplyLineList;

    private String errorMsg;

    private Boolean tenantAdminFlag;

    private String signName;

    private String listLineName;

    private String invoiceNumberFrom;
    private String invoiceNumberTo;
    private String creationDateFrom;
    private String creationDateTo;
    private String submitTimeFrom;
    private String submitTimeTo;
    private String invoiceTypeParam;
    private List<String> listApplyStatus;

}
