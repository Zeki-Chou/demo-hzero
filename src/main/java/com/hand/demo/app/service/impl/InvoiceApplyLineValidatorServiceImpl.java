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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@ImportValidators({
        @ImportValidator(templateCode = "EXAM-47358-HEADER", sheetName = "INVOICE_APPLY_LINE")
})
public class InvoiceApplyLineValidatorServiceImpl extends BatchValidatorHandler {
    @Autowired
    InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public boolean validate(List<String> data) {
        List<InvoiceApplyLine> invoiceApplyLines = new ArrayList<>();

        for (String line : data) {
            if (!line.isEmpty() || line.length() != 0) {
                try {
                    InvoiceApplyLine invoiceApplyLine  = objectMapper.readValue(line, InvoiceApplyLine.class);
                    invoiceApplyLines.add(invoiceApplyLine);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (InvoiceApplyLine lineData: invoiceApplyLines) {
            if (lineData.getApplyLineId() != null) {
                List<InvoiceApplyLine> existNumber = invoiceApplyLineRepository.select(InvoiceApplyLine.FIELD_APPLY_LINE_ID,
                        lineData.getApplyLineId());
                if (existNumber.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
