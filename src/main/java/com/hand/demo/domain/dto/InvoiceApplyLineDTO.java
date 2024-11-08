package com.hand.demo.domain.dto;

import com.hand.demo.domain.entity.InvoiceApplyLine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ExcelSheet(en = "Invoice Apply Line")
public class InvoiceApplyLineDTO extends InvoiceApplyLine {
    @ExcelColumn(en = "Apply Header Number", order = 2)
    private String applyHeaderNumber;
}
