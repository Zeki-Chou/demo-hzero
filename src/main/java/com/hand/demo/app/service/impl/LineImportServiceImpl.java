package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
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

    @Autowired
    InvoiceApplyHeaderService invoiceApplyHeaderService;

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

                    if(invoiceApplyLine1.getApplyHeaderId() != invoiceApplyLineObj.getApplyHeaderId()) {
                        lineIdSet.add(invoiceApplyLine1.getApplyHeaderId());
                    }

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
                invoiceApplyHeaderService.countApplyLineUpdateHeader(listLineId.get(i));
            }
        }

        return flag.get();
    }
}
