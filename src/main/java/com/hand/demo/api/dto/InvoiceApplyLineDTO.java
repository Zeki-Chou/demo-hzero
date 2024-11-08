package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.InvoiceApplyLine;
import lombok.Getter;
import lombok.Setter;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

@Getter
@Setter
//@ExcelSheet(en = "Invoice Line Header")
public class InvoiceApplyLineDTO extends InvoiceApplyLine {
    @ExcelColumn(en = "Header Number", order = 4)
    private String headerNumber;
}
