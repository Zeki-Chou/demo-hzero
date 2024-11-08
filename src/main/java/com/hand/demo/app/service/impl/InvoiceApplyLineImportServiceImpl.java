package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import lombok.AllArgsConstructor;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.hzero.boot.imported.app.service.BatchImportHandler;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

@ImportService(templateCode = "EXAM-47361-HEADER", sheetName = "Invoice_Apply_Line")
@AllArgsConstructor
public class InvoiceApplyLineImportServiceImpl extends BatchImportHandler {
    private final ObjectMapper objectMapper;
    private final InvoiceApplyLineRepository invoiceApplyLineRepository;
    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;


    @Override
    public Boolean doImport(List<String> data) {
        if(data == null || data.isEmpty()){
            return Boolean.FALSE;
        }

        List<InvoiceApplyLine> insertLineList = new LinkedList<>();
        List<InvoiceApplyLine> updateLineList = new LinkedList<>();
        List<InvoiceApplyHeader> listHeaderUpdate = new LinkedList<>();

        try {
            for(String jsonData : data){
                InvoiceApplyLine line = objectMapper.readValue(jsonData, InvoiceApplyLine.class);
                if(line.getApplyLineId() == null){
                    calculateLine(line);
                    line.setTenantId(0L);
                    insertLineList.add(line);
                    InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectByPrimaryKey(line.getApplyHeaderId());
                    if(!listHeaderUpdate.contains(header)){
                        listHeaderUpdate.add(header);
                    }
                }
                InvoiceApplyLine existsLine = invoiceApplyLineRepository.selectByPrimary(line.getApplyLineId());
                if(existsLine != null){
                    if(line.getUnitPrice() == null){
                        line.setUnitPrice(existsLine.getUnitPrice());
                    }
                    if(line.getQuantity() == null){
                        line.setQuantity(existsLine.getQuantity());
                    }
                    if(line.getTaxRate() == null){
                        line.setTaxRate(existsLine.getTaxRate());
                    }
                    calculateLine(line);
                    line.setObjectVersionNumber(existsLine.getObjectVersionNumber());
                    line.setApplyLineId(existsLine.getApplyLineId());
                    updateLineList.add(line);
                    InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectByPrimaryKey(line.getApplyHeaderId());
                    if(!listHeaderUpdate.contains(header)){
                        listHeaderUpdate.add(header);
                    }
                }
            }
            invoiceApplyLineRepository.batchInsertSelective(insertLineList);
            invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateLineList);

            recalculateAndUpdateHeaderTotals(listHeaderUpdate);

            return Boolean.TRUE;

        }catch (Exception e){
            return Boolean.FALSE;
        }
    }

    private void recalculateAndUpdateHeaderTotals(List<InvoiceApplyHeader> listSavedHeader) {
        for(InvoiceApplyHeader savedHeader : listSavedHeader){

            BigDecimal newTotalAmount = BigDecimal.ZERO;
            BigDecimal newExcludeTaxAmount = BigDecimal.ZERO;
            BigDecimal newTaxAmount = BigDecimal.ZERO;

            List<InvoiceApplyLine> updatedLines = invoiceApplyLineRepository.selectByHeaderId(savedHeader.getApplyHeaderId());
            for (InvoiceApplyLine line : updatedLines) {
                newTotalAmount = newTotalAmount.add(line.getTotalAmount() != null ? line.getTotalAmount() : BigDecimal.ZERO);
                newExcludeTaxAmount = newExcludeTaxAmount.add(line.getExcludeTaxAmount() != null ? line.getExcludeTaxAmount() : BigDecimal.ZERO);
                newTaxAmount = newTaxAmount.add(line.getTaxAmount() != null ? line.getTaxAmount() : BigDecimal.ZERO);
            }

            savedHeader.setTotalAmount(newTotalAmount);
            savedHeader.setExcludeTaxAmount(newExcludeTaxAmount);
            savedHeader.setTaxAmount(newTaxAmount);

            invoiceApplyHeaderRepository.updateByPrimaryKeySelective(savedHeader);
        }
    }

    public void calculateLine(InvoiceApplyLine invoiceApplyLine){
        invoiceApplyLine.setTotalAmount(invoiceApplyLine.getQuantity().multiply(invoiceApplyLine.getUnitPrice()));
        invoiceApplyLine.setTaxAmount(invoiceApplyLine.getTotalAmount().multiply(invoiceApplyLine.getTaxRate()));
        invoiceApplyLine.setExcludeTaxAmount(invoiceApplyLine.getTotalAmount().subtract(invoiceApplyLine.getTaxAmount()));
    }
}
