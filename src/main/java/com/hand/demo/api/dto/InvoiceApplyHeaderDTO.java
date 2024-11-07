package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import lombok.Getter;
import lombok.Setter;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

import java.util.List;

@Getter
@Setter
@ExcelSheet(en = "Invoice Apply Header")
public class InvoiceApplyHeaderDTO extends InvoiceApplyHeader {
    @ExcelColumn(en = "Lines", order = 16)
    private List<InvoiceApplyLine> invoiceApplyLines;
    @ExcelColumn(en = "Status Meaning", order = 13)
    private String applyStatusMeaning;
    @ExcelColumn(en = "Color Meaning", order = 14)
    private String invoiceColorMeaning;
    @ExcelColumn(en = "Type Meaning", order = 15)
    private String invoiceTypeMeaning;
}
