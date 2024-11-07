package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.InvoiceApplyLine;
import lombok.Getter;
import lombok.Setter;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

@Getter
@Setter
@ExcelSheet(en = "Invoice Apply Line")
public class InvoiceApplyLineDTO extends InvoiceApplyLine {
    @ExcelColumn(en = "Apply Header Number", order = 14)
    private String applyHeaderNumber;
}
