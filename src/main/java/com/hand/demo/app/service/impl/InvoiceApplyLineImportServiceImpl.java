package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.util.Utils;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ImportService(templateCode = "EXAM-47359-HEADER", sheetIndex = 1)
public class InvoiceApplyLineImportServiceImpl extends BatchImportHandler {

    private final ObjectMapper objectMapper;
    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    private final InvoiceApplyLineRepository invoiceApplyLineRepository;

    public InvoiceApplyLineImportServiceImpl(ObjectMapper objectMapper, InvoiceApplyHeaderRepository invoiceApplyHeaderRepository, InvoiceApplyLineRepository invoiceApplyLineRepository) {
        this.objectMapper = objectMapper;
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
        this.invoiceApplyLineRepository = invoiceApplyLineRepository;
    }

    @Override
    public Boolean doImport(List<String> data) {
        List<InvoiceApplyLine> invoiceApplyLines = new ArrayList<>();
        for (String d: data) {
            try {
                InvoiceApplyLine invoiceApplyLine = objectMapper.readValue(d, InvoiceApplyLine.class);
                BigDecimal totalAmount = invoiceApplyLine.getUnitPrice().multiply(invoiceApplyLine.getQuantity());
                BigDecimal taxAmount = totalAmount.multiply(invoiceApplyLine.getTaxRate());
                BigDecimal excludeTaxAmount = totalAmount.subtract(taxAmount);

                invoiceApplyLine.setTotalAmount(totalAmount);
                invoiceApplyLine.setTaxAmount(taxAmount);
                invoiceApplyLine.setExcludeTaxAmount(excludeTaxAmount);

                InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectByPrimary(invoiceApplyLine.getApplyHeaderId());

                // new invoice line
                if (invoiceApplyLine.getApplyLineId() == null) {
                    BigDecimal sumTotalAmount = header.getTotalAmount().add(totalAmount);
                    BigDecimal sumTaxAccount = header.getTaxAmount().add(taxAmount);
                    BigDecimal sumExcludeTaxAccount = header.getExcludeTaxAmount().add(excludeTaxAmount);

                    header.setTotalAmount(sumTotalAmount);
                    header.setTaxAmount(sumTaxAccount);
                    header.setExcludeTaxAmount(sumExcludeTaxAccount);
                } else {
                    // update invoice line: need to calculate the diff between updated line and the one from database
                    InvoiceApplyLine prevInvoiceLine = invoiceApplyLineRepository.selectByPrimary(invoiceApplyLine.getApplyLineId());
                    InvoiceApplyLine invoiceLineDiff = Utils.invoiceApplyLineDiff(invoiceApplyLine, prevInvoiceLine);

                    BigDecimal newTotalAmount = header.getTotalAmount().add(invoiceLineDiff.getTotalAmount());
                    BigDecimal newTaxAmount = header.getTaxAmount().add(invoiceLineDiff.getTaxAmount());
                    BigDecimal newExcludeTaxAmount = header.getExcludeTaxAmount().add(invoiceLineDiff.getExcludeTaxAmount());

                    header.setTotalAmount(newTotalAmount);
                    header.setTaxAmount(newTaxAmount);
                    header.setExcludeTaxAmount(newExcludeTaxAmount);

                    // prevent optimistic lock
                    invoiceApplyLine.setObjectVersionNumber(prevInvoiceLine.getObjectVersionNumber());
                }

                invoiceApplyHeaderRepository.updateByPrimaryKeySelective(header);
                invoiceApplyLines.add(invoiceApplyLine);
            } catch (JsonProcessingException e) {
                getContextList().get(0).addErrorMsg("Error processing JSON context");
                return Boolean.FALSE;
            }
        }

        List<InvoiceApplyLine> insertList = invoiceApplyLines.stream().filter(line -> line.getApplyLineId() == null).collect(Collectors.toList());
        List<InvoiceApplyLine> updateList = invoiceApplyLines.stream().filter(line -> line.getApplyLineId() != null).collect(Collectors.toList());
        invoiceApplyLineRepository.batchInsertSelective(insertList);
        invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);

        return Boolean.TRUE;
    }
}
