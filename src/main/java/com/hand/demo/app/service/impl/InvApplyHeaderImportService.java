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
import org.hzero.mybatis.domian.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
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

        Set<String> headerNumbers = updateList.stream().map(InvoiceApplyHeader::getApplyHeaderNumber).collect(Collectors.toSet());
        Condition condition = new Condition(InvoiceApplyHeader.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andIn(InvoiceApplyHeader.FIELD_APPLY_HEADER_NUMBER, headerNumbers);

        List<InvoiceApplyHeader> headerFromDbToUpdate = invoiceApplyHeaderRepository.selectByCondition(condition);

//        {headerNumber1: {HeaderObject2}, headerNumber2: {HeaderObject2}}
        Map<String, InvoiceApplyHeader> headerByApplyHeaderNumber = headerFromDbToUpdate.stream()
                .collect(Collectors.toMap(InvoiceApplyHeader::getApplyHeaderNumber, Function.identity()));

        List<InvoiceApplyHeader> updateListWithPrimaryKey = updateList.stream()
                .peek(header -> {
                    InvoiceApplyHeader headerFromDB = headerByApplyHeaderNumber.get(header.getApplyHeaderNumber());
                    header.setApplyHeaderId(headerFromDB.getApplyHeaderId());
                    header.setObjectVersionNumber(headerFromDB.getObjectVersionNumber());
                })
                .collect(Collectors.toList());

        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateListWithPrimaryKey);

        return flag.get();
    }
}