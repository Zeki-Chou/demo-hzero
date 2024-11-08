package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.domain.entity.ImportData;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ImportValidators({
        @ImportValidator(templateCode = "INVOICE-APPLY-HEADER-47360",sheetName = "Line",sheetIndex = 1)
})
public class ImportInvoiceApplyLineValidate  extends BatchValidatorHandler {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LovAdapter lovAdapter;
    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Override
    public boolean validate(List<String> data) {
        List<InvoiceApplyLine> invoiceApplyLines = new ArrayList<>();
        for (int i=0;i<data.size();i++) {
            try {
                InvoiceApplyLine invoiceApplyLine = objectMapper.readValue(data.get(i), InvoiceApplyLine.class);
                invoiceApplyLines.add(invoiceApplyLine);
            } catch (Exception e) {
                getContext().get(i).setErrorMsg("Error parsing data");
                return false;
            }
        }

        validateHeaderExistence(invoiceApplyLines,getContext());
        validateLineExistence(invoiceApplyLines,getContext());

        return getContext().stream().noneMatch(ctx -> ctx.getErrorMsg() != null && !ctx.getErrorMsg().isEmpty());
    }

    private  void  validateHeaderExistence(List<InvoiceApplyLine> invoiceApplyLines, List<ImportData> importDataCtx){
        String headerIds = invoiceApplyLines.stream().map(line -> line.getApplyHeaderId().toString()).collect(Collectors.joining(","));
        if(headerIds.isEmpty()){
            return;
        }

        Set<String> foundHeaderIds = invoiceApplyHeaderRepository.selectByIds(headerIds).stream().map(line->line.getApplyHeaderId().toString()).collect(Collectors.toSet());
        for(int i=0;i<invoiceApplyLines.size();i++){
            if(!foundHeaderIds.contains(invoiceApplyLines.get(i).getApplyHeaderId().toString())){
                importDataCtx.get(i).setErrorMsg("Fail Validate Header Exist");
            }
        }
    }

    private void validateLineExistence(List<InvoiceApplyLine> invoiceApplyLines, List<ImportData> importDataCtx){
        String lineIds = invoiceApplyLines.stream().filter(line->line.getApplyLineId()!=null).map(line -> line.getApplyLineId().toString()).collect(Collectors.joining(","));
        if(lineIds.isEmpty()){
            return;
        }

        Set<String> foundLineIds = invoiceApplyLineRepository.selectByIds(lineIds).stream().map(line->line.getApplyLineId().toString()).collect(Collectors.toSet());
        for(int i=0;i<invoiceApplyLines.size();i++){
            if(invoiceApplyLines.get(i).getApplyLineId() != null && !foundLineIds.contains(invoiceApplyLines.get(i).getApplyLineId().toString())){
                importDataCtx.get(i).setErrorMsg("Fail Validate Line Exist");
            }
        }
    }
}
