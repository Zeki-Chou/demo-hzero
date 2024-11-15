package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@ImportValidators({
        @ImportValidator(templateCode = "EXAM-47358-HEADER", sheetName = "INVOICE_APPLY_HEADER")
})
public class InvoiceApplyHeaderValidatorServiceImpl extends BatchValidatorHandler {
    @Autowired
    InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public boolean validate(List<String> data) {
        List<InvoiceApplyHeader> invoiceApplyHeaders = new ArrayList<>();

        for (String header : data) {
            if (!header.isEmpty() || header.length() != 0) {
                try {
                    InvoiceApplyHeader invoiceApplyHeader = objectMapper.readValue(header, InvoiceApplyHeader.class);
                    invoiceApplyHeaders.add(invoiceApplyHeader);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (InvoiceApplyHeader headerData: invoiceApplyHeaders) {
            if (headerData.getApplyHeaderNumber() != null) {
                List<InvoiceApplyHeader> existNumber = invoiceApplyHeaderRepository.select("applyHeaderNumber",
                        headerData.getApplyHeaderNumber());
                if (existNumber.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
