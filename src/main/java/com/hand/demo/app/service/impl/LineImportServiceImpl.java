package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;

import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;
import org.opensaml.xml.signature.P;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@ImportValidators(
        @ImportValidator(templateCode = "EXAM-47356-HEADER", sheetIndex = 1)
)
public class LineImportServiceImpl extends BatchValidatorHandler {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    public boolean validate(List<String> data) {
        AtomicBoolean flag = new AtomicBoolean(true);

        List<InvoiceApplyLine> lineListInsert = new ArrayList<>();
        List<InvoiceApplyLine> lineListUpdate = new ArrayList<>();
        Set<Long> lineIdSet = new HashSet<>();

        AtomicInteger index = new AtomicInteger(0);
        data.forEach(invoiceApplyLine -> {
            int i = index.getAndIncrement();

            try{
                InvoiceApplyLine invoiceApplyLineObj = objectMapper.readValue(invoiceApplyLine, InvoiceApplyLine.class);
                InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(invoiceApplyLineObj.getApplyHeaderId());
                if(invoiceApplyHeader == null) {
                    getContext().get(i).addErrorMsg("Error applyLine : Header Id Not Found !");
                    flag.set(false);
                }
            } catch (JsonProcessingException e) {
                getContext().get(i).addErrorMsg("Fail to read data");
                flag.set(false);
            }
        });

        AtomicInteger index1 = new AtomicInteger(0);
        data.forEach(invoiceApplyLine -> {
            int i = index1.getAndIncrement();

            try{
                InvoiceApplyLine invoiceApplyLineObj = objectMapper.readValue(invoiceApplyLine, InvoiceApplyLine.class);
                InvoiceApplyLine invoiceApplyLine1 = invoiceApplyLineRepository.selectByPrimary(invoiceApplyLineObj.getApplyLineId());

                BigDecimal totalAmount = invoiceApplyLineObj.getUnitPrice().multiply(invoiceApplyLineObj.getQuantity());
                BigDecimal taxAmount = totalAmount.multiply(invoiceApplyLineObj.getTaxRate());
                BigDecimal excludeTaxAmount = totalAmount.subtract(taxAmount);

                if(invoiceApplyLine1 != null) {
                    invoiceApplyLineObj.setTotalAmount(totalAmount);
                    invoiceApplyLineObj.setTaxAmount(taxAmount);
                    invoiceApplyLineObj.setExcludeTaxAmount(excludeTaxAmount);

                    invoiceApplyLineObj.setObjectVersionNumber(invoiceApplyLine1.getObjectVersionNumber());

                    lineListUpdate.add(invoiceApplyLineObj);
                } else {
                    invoiceApplyLineObj.setTotalAmount(totalAmount);
                    invoiceApplyLineObj.setTaxAmount(taxAmount);
                    invoiceApplyLineObj.setExcludeTaxAmount(excludeTaxAmount);

                    lineListInsert.add(invoiceApplyLineObj);
                }
                lineIdSet.add(invoiceApplyLineObj.getApplyHeaderId());
            } catch (JsonProcessingException e) {
                getContext().get(i).addErrorMsg("Fail to read data");
                flag.set(false);
            }
        });

        if (flag.get()) {
            invoiceApplyLineRepository.batchInsertSelective(lineListInsert);
            invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(lineListUpdate);

            List<Long> listLineId = new ArrayList<>(lineIdSet);
            for(int i = 0; i < listLineId.size(); i++) {
                InvoiceApplyLine invoiceApplyLine = new InvoiceApplyLine();
                invoiceApplyLine.setApplyHeaderId(listLineId.get(i));

                BigDecimal headerTaxAmount = BigDecimal.ZERO;
                BigDecimal headerExcludeTaxAmount = BigDecimal.ZERO;
                BigDecimal headerTotalAmount = BigDecimal.ZERO;

                List<InvoiceApplyLine> invoiceApplyLineList = invoiceApplyLineRepository.select(invoiceApplyLine);
                for(int p = 0; p < invoiceApplyLineList.size(); p++) {
                    InvoiceApplyLine invoiceApplyLine1 = invoiceApplyLineList.get(p);

                    BigDecimal taxAmount = invoiceApplyLine1.getTaxAmount() != null ? invoiceApplyLine1.getTaxAmount() : BigDecimal.ZERO;
                    BigDecimal excludeTaxAmount = invoiceApplyLine1.getExcludeTaxAmount() != null ? invoiceApplyLine1.getExcludeTaxAmount() : BigDecimal.ZERO;
                    BigDecimal totalAmount = invoiceApplyLine1.getTotalAmount() != null ? invoiceApplyLine1.getTotalAmount() : BigDecimal.ZERO;

                    headerTaxAmount = headerTaxAmount.add(taxAmount);
                    headerExcludeTaxAmount = headerExcludeTaxAmount.add(excludeTaxAmount);
                    headerTotalAmount = headerTotalAmount.add(totalAmount);
                }

                InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(listLineId.get(i));
                invoiceApplyHeader.setTaxAmount(headerTaxAmount);
                invoiceApplyHeader.setExcludeTaxAmount(headerExcludeTaxAmount);
                invoiceApplyHeader.setTotalAmount(headerTotalAmount);

                invoiceApplyHeaderRepository.updateByPrimaryKeySelective(invoiceApplyHeader);
            }
        }

        return flag.get();
    }
}
