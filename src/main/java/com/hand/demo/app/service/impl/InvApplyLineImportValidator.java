package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import lombok.AllArgsConstructor;
import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;
import org.hzero.mybatis.domian.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@ImportValidators({
        @ImportValidator(templateCode = "EXAM-47355-APPLY-HEADER", sheetName = "invoice-apply-line")
})
@AllArgsConstructor
public class InvApplyLineImportValidator extends BatchValidatorHandler {

    private ObjectMapper objectMapper;

    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Override
    public boolean validate(List<String> data) {
        if (data == null || data.isEmpty()) {
            getContext().get(0).setBackInfo("Data empty");
            return Boolean.FALSE;
        }

//        AtomicBoolean flag = new AtomicBoolean(true);

        List<Long> applyLineIds = new ArrayList<>();
        List<Long> applyHeaderIds = new ArrayList<>();

        for (String json : data) {
            try {
                InvoiceApplyLine line = objectMapper.readValue(json, InvoiceApplyLine.class);

                if(line.getApplyLineId() != null) {
                    applyLineIds.add(line.getApplyLineId());
                }
                if(line.getApplyHeaderId() != null) {
                    applyHeaderIds.add(line.getApplyHeaderId());
                }
            } catch (JsonProcessingException e) {
                getContext().get(0).setBackInfo(e.getMessage());
                return Boolean.FALSE;
            }
        }

        if(!applyHeaderIds.isEmpty()) {
            Condition headerCondition = new Condition(InvoiceApplyHeader.class);
            Condition.Criteria criteria = headerCondition.createCriteria();
            criteria.andEqualTo("delFlag", 0).andIn("applyHeaderId", applyHeaderIds);

            List<InvoiceApplyHeader> invApplyHeaders = invoiceApplyHeaderRepository.selectByCondition(headerCondition);

            Set<Long> existingApplyHeaderIds = invApplyHeaders.stream()
                    .map(InvoiceApplyHeader::getApplyHeaderId)
                    .collect(Collectors.toSet());

            if(!existingApplyHeaderIds.containsAll(applyHeaderIds)) {
                getContext().get(0).setBackInfo("applyHeaderId not exist in database");
                return Boolean.FALSE;
            }
        }

        if(!applyLineIds.isEmpty()) {
            Condition lineCondition = new Condition(InvoiceApplyLine.class);
            Condition.Criteria criteria = lineCondition.createCriteria();
            criteria.andIn("applyLineId", applyLineIds);

            List<InvoiceApplyLine> invApplyLines = invoiceApplyLineRepository.selectByCondition(lineCondition);

            Set<Long> existingApplyLineIds = invApplyLines.stream()
                    .map(InvoiceApplyLine::getApplyLineId)
                    .collect(Collectors.toSet());

            if(!existingApplyLineIds.containsAll(applyLineIds)) {
                getContext().get(0).setBackInfo("applyLineId not exist in database");
                return Boolean.FALSE;
            }
        }

        return Boolean.TRUE;
    }
}
