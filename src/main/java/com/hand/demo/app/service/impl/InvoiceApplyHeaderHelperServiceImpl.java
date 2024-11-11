package com.hand.demo.app.service.impl;


import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import org.hzero.mybatis.domian.Condition;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class InvoiceApplyHeaderHelperServiceImpl {

    private final InvoiceApplyLineRepository invoiceApplyLineRepository;
    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    public InvoiceApplyHeaderHelperServiceImpl(InvoiceApplyLineRepository invoiceApplyLineRepository, InvoiceApplyHeaderRepository invoiceApplyHeaderRepository) {
        this.invoiceApplyLineRepository = invoiceApplyLineRepository;
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
    }

    //Process the invoice lines for header
    public void processInvoiceLines(List<InvoiceApplyLine> invoiceLines, InvoiceApplyHeader header) {
        BigDecimal headerTotalAmount = BigDecimal.ZERO;
        BigDecimal headerExcludeTaxAmount = BigDecimal.ZERO;
        BigDecimal headerTaxAmount = BigDecimal.ZERO;

        if (invoiceLines != null && !invoiceLines.isEmpty()) {
            for (InvoiceApplyLine line : invoiceLines) {
                // Set the applyHeaderId from the parent header
                line.setApplyHeaderId(header.getApplyHeaderId());

                // Calculate line-level fields
                BigDecimal lineTotalAmount = line.getUnitPrice().multiply(line.getQuantity());
                line.setTotalAmount(lineTotalAmount);

                BigDecimal lineTaxAmount = lineTotalAmount.multiply(line.getTaxRate());
                line.setTaxAmount(lineTaxAmount);

                BigDecimal lineExcludeTaxAmount = lineTotalAmount.subtract(lineTaxAmount);
                line.setExcludeTaxAmount(lineExcludeTaxAmount);

                // Accumulate to header-level totals
                headerTotalAmount = headerTotalAmount.add(lineTotalAmount);
                headerExcludeTaxAmount = headerExcludeTaxAmount.add(lineExcludeTaxAmount);
                headerTaxAmount = headerTaxAmount.add(lineTaxAmount);
            }

            // Save lines to the database
            invoiceApplyLineRepository.batchInsert(invoiceLines);
        }

        // Set header-level totals after processing all lines
        header.setTotalAmount(headerTotalAmount);
        header.setExcludeTaxAmount(headerExcludeTaxAmount);
        header.setTaxAmount(headerTaxAmount);
    }

    public Optional<InvoiceApplyHeader> findExistingHeader(String applyHeaderNumber) {
        Condition condition = new Condition(InvoiceApplyHeader.class);
        condition.createCriteria().andEqualTo("applyHeaderNumber", applyHeaderNumber);
        return invoiceApplyHeaderRepository.selectByCondition(condition).stream().findFirst();
    }



}
