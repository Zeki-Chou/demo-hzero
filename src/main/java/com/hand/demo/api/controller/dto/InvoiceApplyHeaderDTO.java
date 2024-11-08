package com.hand.demo.api.controller.dto;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import lombok.Data;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

import java.util.List;

@Data
@ExcelSheet(en = "InvoiceApplyHeader")
public class InvoiceApplyHeaderDTO extends InvoiceApplyHeader {
    private List<InvoiceApplyLine> dataList;

    @ExcelColumn(en = "invoice type meaning")
    private String invoiceTypeMeaning;

    @ExcelColumn(en = "apply status meaning")
    private String applyStatusMeaning;

    @ExcelColumn(en = "invoice color meaning")
    private String invoiceColorMeaning;
}
