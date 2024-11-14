package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;

import java.util.ArrayList;
import java.util.List;

@ImportService(templateCode = "EXAM-47359-HEADER", sheetIndex = 1)
public class InvoiceApplyLineImportServiceImpl extends BatchImportHandler {

    private final ObjectMapper objectMapper;
    private final InvoiceApplyLineService invoiceApplyLineService;

    public InvoiceApplyLineImportServiceImpl(ObjectMapper objectMapper, InvoiceApplyLineService invoiceApplyLineService) {
        this.objectMapper = objectMapper;
        this.invoiceApplyLineService = invoiceApplyLineService;
    }

    @Override
    public Boolean doImport(List<String> data) {
        List<InvoiceApplyLine> invoiceApplyLines = new ArrayList<>();
        for (String d: data) {
            try {
                InvoiceApplyLine invoiceApplyLine = objectMapper.readValue(d, InvoiceApplyLine.class);
                invoiceApplyLines.add(invoiceApplyLine);
            } catch (JsonProcessingException e) {
                getContextList().get(0).addErrorMsg("Error processing JSON context");
                return Boolean.FALSE;
            }
        }

        invoiceApplyLineService.saveData(invoiceApplyLines);

        return Boolean.TRUE;
    }
}
