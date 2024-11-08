package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.InvoiceApplyLine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ExcelSheet(en = "Invoice Apply Line")
public class InvoiceApplyLineDto extends InvoiceApplyLine {

    @ExcelColumn(en = "applyHeaderNumber")
    private String applyHeaderNumber;
}
