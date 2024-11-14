package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.controller.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;

import java.util.ArrayList;
import java.util.List;

@ImportService(templateCode = "EXAM-47359-HEADER")
public class InvoiceApplyHeaderImportServiceImpl extends BatchImportHandler {

    private final ObjectMapper objectMapper;
    private final InvoiceApplyHeaderService invoiceApplyHeaderService;

    public InvoiceApplyHeaderImportServiceImpl(ObjectMapper objectMapper, InvoiceApplyHeaderService invoiceApplyHeaderService) {
        this.objectMapper = objectMapper;
        this.invoiceApplyHeaderService = invoiceApplyHeaderService;
    }

    @Override
    public Boolean doImport(List<String> data) {
        List<InvoiceApplyHeaderDTO> headers = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            try {
                InvoiceApplyHeaderDTO header = objectMapper.readValue(data.get(i), InvoiceApplyHeaderDTO.class);
                headers.add(header);
            } catch (JsonProcessingException e) {
                getContextList().get(i).addErrorMsg("Error Processing JSON Context");
            }
        }

        invoiceApplyHeaderService.saveData(headers, 0L);

        return Boolean.TRUE;
    }
}
