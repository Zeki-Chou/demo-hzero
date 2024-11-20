package com.hand.demo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hzero.boot.platform.lov.annotation.LovValue;

import javax.persistence.Column;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceApplyInfoDTOOut {
    private String invoiceNumber;

    @LovValue(lovCode = "DEMO-47356.INV_APPLY_HEADER.APPLY_STATUS")
    private String applyStatus;

    private Date submitTime;

    @LovValue(lovCode = "DEMO-47356.INV_APPLY_HEADER.INV_COLOR")
    private String invoiceColor;

    @LovValue(lovCode = "DEMO-47356.INV_APPLY_HEADER.INV_TYPE")
    private String invoiceType;

    private Integer totalAmount;
    private Integer excludeTaxAmount;
    private Integer taxAmount;
    private String invoiceName;

    private String applyStatusMeaning;
    private String invoiceColorMeaning;
    private String invoiceTypeMeaning;
}