package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.InvoiceApplyLine;
import lombok.Getter;
import lombok.Setter;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

@Getter
@Setter
@ExcelSheet(en="Invoice Line")
public class InvoiceApplyLineDTO extends InvoiceApplyLine {
    @ExcelColumn(order = 3)
    private String invoiceApplyHeaderNumber;
}
