package com.hand.demo.api.controller.dto;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import lombok.Data;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

import java.util.Date;
import java.util.List;

@Data
@ExcelSheet(en = "InvoiceApplyHeader")
public class InvoiceApplyHeaderDTO extends InvoiceApplyHeader {
    @ExcelColumn(en = "invoice type meaning")
    private String invoiceTypeMeaning;

    @ExcelColumn(en = "apply status meaning")
    private String applyStatusMeaning;

    @ExcelColumn(en = "invoice color meaning")
    private String invoiceColorMeaning;

    private List<InvoiceApplyLine> dataList;

    private Boolean tenantAdminFlag;

    private Date fromSubmitTime;

    private Date toSubmitTime;

    private Date fromCreationDate;

    private Date toCreationDate;

    private String fromApplyHeaderNumber;

    private String toApplyHeaderNumber;

    private List<String> applyStatusList;

    private String realName;

    private String invoiceNames;

}
