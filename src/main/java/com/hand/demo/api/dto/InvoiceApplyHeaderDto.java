package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

import javax.persistence.Table;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ExcelSheet(en = "Invoice Apply Header New")
public class InvoiceApplyHeaderDto extends InvoiceApplyHeader {

    @ExcelColumn(en = "applyStatusMeaning", order=16)
    private String applyStatusMeaning;

    @ExcelColumn(en = "invoiceColorMeaning", order=17)
    private String invoiceColorMeaning;

    @ExcelColumn(en = "invoiceLineList", order=18)
    private String invoiceTypeMeaning;

    @ExcelColumn(en = "invoiceLineList", child = true, order=19)
    private List<InvoiceApplyLine> invoiceLineList;
}
