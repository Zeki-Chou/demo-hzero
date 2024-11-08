package com.hand.demo.api.controller.dto;

import com.hand.demo.domain.entity.InvoiceApplyLine;
import lombok.Data;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

@Data
@ExcelSheet(en = "InvoiceApplyLine")
public class InvoiceApplyLineDTO extends InvoiceApplyLine {
    @ExcelColumn(en = "apply header number")
    String applyHeaderNumber;
}
