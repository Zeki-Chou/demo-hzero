package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InvoiceApplyHeaderDTO extends InvoiceApplyHeader {
    private List<InvoiceApplyLine> invoiceApplyLines;
    private String applyStatusMeaning;
    private String invoiceColorMeaning;
    private String invoiceTypeMeaning;
}
