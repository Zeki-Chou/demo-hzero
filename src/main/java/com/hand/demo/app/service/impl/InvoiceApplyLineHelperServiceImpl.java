package com.hand.demo.app.service.impl;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InvoiceApplyLineHelperServiceImpl {
    public void setLineAmounts(InvoiceApplyLine line) {
        line.setTotalAmount(line.getUnitPrice().multiply(line.getQuantity()));
        line.setTaxAmount(line.getTotalAmount().multiply(line.getTaxRate()));
        line.setExcludeTaxAmount(line.getTotalAmount().subtract(line.getTaxAmount()));
    }

    public void updateHeaderWithLineTotals(InvoiceApplyHeader header, List<InvoiceApplyLine> lines) {
        BigDecimal totalAmountSum = lines.stream()
                .map(InvoiceApplyLine::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxAmountSum = lines.stream()
                .map(InvoiceApplyLine::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal excludeTaxAmountSum = lines.stream()
                .map(InvoiceApplyLine::getExcludeTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        header.setTotalAmount(totalAmountSum);
        header.setTaxAmount(taxAmountSum);
        header.setExcludeTaxAmount(excludeTaxAmountSum);

    }

}
