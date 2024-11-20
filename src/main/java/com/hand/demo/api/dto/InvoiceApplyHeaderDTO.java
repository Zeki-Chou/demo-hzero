package com.hand.demo.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.infra.constant.InvoiceApplyConstants;
import lombok.Getter;
import lombok.Setter;
import org.hzero.boot.platform.lov.annotation.LovValue;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@ExcelSheet(en = "Invoice Apply Header")
public class InvoiceApplyHeaderDTO extends InvoiceApplyHeader {
    private List<InvoiceApplyLine> headerLines;

    @ExcelColumn(en = "Apply Status Meaning", lovCode = InvoiceApplyConstants.INV_APPLY_HEADER_APPLY_STATUS, order = 17)
    private String applyStatusMeaning;

    @ExcelColumn(en = "Invoice Color Meaning", lovCode = InvoiceApplyConstants.INV_APPLY_HEADER_INV_COLOR, order = 18)
    private String invoiceColorMeaning;

    @ExcelColumn(en = "Invoice Type Meaning", lovCode = InvoiceApplyConstants.INV_APPLY_HEADER_INV_TYPE, order = 19)
    private String invoiceTypeMeaning;

    @JsonIgnore
    private Boolean isAdminFlag;

    private String sign;

    private String invoiceName;
}
