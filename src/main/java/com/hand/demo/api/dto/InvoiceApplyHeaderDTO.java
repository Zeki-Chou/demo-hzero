package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.infra.constant.Constants;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hzero.boot.platform.lov.annotation.LovValue;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

import java.util.List;

@Getter
@Setter
@ExcelSheet(en="Invoice Header")
public class InvoiceApplyHeaderDTO extends InvoiceApplyHeader {
    @ExcelColumn(en = "Invoice Color Meaning", order = 9)
    private String invoiceColorMeaning;
    @ExcelColumn(en = "Invoice Type Meaning", order = 10)
    private String invoiceTypeMeaning;
    @ExcelColumn(en = "Apply Status Meaning", order = 11)
    private String applyStatusMeaning;
    @ExcelColumn(en = "Invoice Apply Line List", order = 12)
    private List<InvoiceApplyLine> invoiceApplyLineList;
    private Boolean tenantAdminFlag;
    private String lineInvoiceNames;
}
