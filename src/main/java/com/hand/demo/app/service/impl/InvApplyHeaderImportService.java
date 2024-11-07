package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.TaskConstants;
import lombok.AllArgsConstructor;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@AllArgsConstructor
@ImportService(templateCode = "EXAM-47355-APPLY-HEADER", sheetName = "invoice-apply-header")
public class InvApplyHeaderImportService extends BatchImportHandler {

    private ObjectMapper objectMapper;

    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    private CodeRuleBuilder codeRuleBuilder;

    @Override
    public Boolean doImport(List<String> data) {
        List<InvoiceApplyHeader> headers = new ArrayList<>();
        AtomicBoolean flag = new AtomicBoolean(true);
        for (String json : data) {
            try {
                InvoiceApplyHeader header = objectMapper.readValue(json, InvoiceApplyHeader.class);
                headers.add(header);
            } catch (JsonProcessingException e) {
                flag.set(false);
                return flag.get();
            }
        }

        List<InvoiceApplyHeader> insertList = headers.stream()
                .filter(header -> header.getApplyHeaderNumber() == null)
                .collect(Collectors.toList());

        List<String> batchCode = codeRuleBuilder.generateCode(insertList.size(), TaskConstants.CODE_RULE, null);

        for(int i = 0; i < insertList.size(); i++) {
            InvoiceApplyHeader invoiceApplyHeader = insertList.get(i);
            invoiceApplyHeader.setApplyHeaderNumber(batchCode.get(i));
        }

        List<InvoiceApplyHeader> updateList = headers.stream()
                .filter(header -> header.getApplyHeaderNumber() != null)
                .collect(Collectors.toList());
        invoiceApplyHeaderRepository.batchInsertSelective(insertList);
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateList);
        return flag.get();
    }
}
