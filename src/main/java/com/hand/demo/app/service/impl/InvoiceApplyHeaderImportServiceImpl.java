package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.CodeRuleConstant;
import lombok.AllArgsConstructor;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;

import java.util.LinkedList;
import java.util.List;



@ImportService(templateCode = "EXAM-47361-HEADER", sheetName = "Invoice_Apply_Header")
@AllArgsConstructor
public class InvoiceApplyHeaderImportServiceImpl extends BatchImportHandler {
    private ObjectMapper objectMapper;
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    private CodeRuleBuilder codeRuleBuilder;

    @Override
    public Boolean doImport(List<String> data) {
        if(data == null || data.isEmpty()){
            return Boolean.FALSE;
        }

        List<InvoiceApplyHeader> insertHeaderList = new LinkedList<>();
        List<InvoiceApplyHeader> updateHeaderList = new LinkedList<>();
        try {
            for(String jsonData : data){
                InvoiceApplyHeader header = objectMapper.readValue(jsonData, InvoiceApplyHeader.class);
                if(header.getApplyHeaderNumber() == null){
                    header.setDelFlag(0);
                    String batchCode = codeRuleBuilder.generateCode(CodeRuleConstant.CODE_RULE_HEADER_NUMBER, null);
                    header.setApplyHeaderNumber(batchCode);
                    header.setTenantId(0L);
                    insertHeaderList.add(header);
                }
                List<InvoiceApplyHeader> existsHeader = invoiceApplyHeaderRepository.select("applyHeaderNumber", header.getApplyHeaderNumber());
                if(!existsHeader.isEmpty()){
                    header.setObjectVersionNumber(existsHeader.get(0).getObjectVersionNumber());
                    header.setApplyHeaderId(existsHeader.get(0).getApplyHeaderId());
                    updateHeaderList.add(header);
                }
            }
            invoiceApplyHeaderRepository.batchInsertSelective(insertHeaderList);
            invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateHeaderList);
            return Boolean.TRUE;
        }catch (Exception e){
            return Boolean.FALSE;
        }
    }
}
