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
public class InvoiceApplyHeaderDto extends InvoiceApplyHeader {

    private String applyStatusMeaning;

    private String invoiceColorMeaning;

    private String invoiceTypeMeaning;

    private List<InvoiceApplyLine> invoiceLineList;
}
