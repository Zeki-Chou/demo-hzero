package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.InvoiceApplyConstants;
import io.choerodon.core.exception.CommonException;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import org.hzero.mybatis.domian.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@ImportValidators({
        @ImportValidator(templateCode = "EXAM-47355-APPLY-HEADER")
})
@AllArgsConstructor
public class InvApplyHeaderImportValidator extends BatchValidatorHandler {

    private ObjectMapper objectMapper;

    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    private LovAdapter lovAdapter;

    @Override
    public boolean validate(List<String> data) {
        if (data == null || data.isEmpty()) {
            getContext().get(0).addErrorMsg("Data empty");
            return Boolean.FALSE;
        }

//        AtomicBoolean flag = new AtomicBoolean(true);

        List<String> applyHeaderNumbers = new ArrayList<>();

        for (String json : data) {
            try {
                InvoiceApplyHeader header = objectMapper.readValue(json, InvoiceApplyHeader.class);
                boolean dataValid = valueSetValidation(header.getApplyStatus(), header.getInvoiceType(), header.getInvoiceColor());
                if(!dataValid) {
                    getContext().get(0).addErrorMsg("Apply status/Invoice type/Invoice color not valid");
                    return Boolean.FALSE;
                }

                if(header.getApplyHeaderNumber() != null) {
                    applyHeaderNumbers.add(header.getApplyHeaderNumber());
                }
            } catch (JsonProcessingException e) {
                getContext().get(0).addErrorMsg(e.getMessage());
                return Boolean.FALSE;
            }
        }

        if(!applyHeaderNumbers.isEmpty()) {
            Condition condition = new Condition(InvoiceApplyHeader.class);
            Condition.Criteria criteria = condition.createCriteria();
            criteria.andIn("applyHeaderNumber", applyHeaderNumbers);

            List<InvoiceApplyHeader> invApplyHeaders = invoiceApplyHeaderRepository.selectByCondition(condition);

            Set<String> existingApplyHeaderNumbers = invApplyHeaders.stream()
                    .map(InvoiceApplyHeader::getApplyHeaderNumber)
                    .collect(Collectors.toSet());

            if (!existingApplyHeaderNumbers.containsAll(applyHeaderNumbers)) {
                getContext().get(0).addErrorMsg("applyheaderNumber not exist in database");
                return Boolean.FALSE;
            }
        }

        return Boolean.TRUE;
    }

    private boolean valueSetValidation(String applyStatus, String invoiceType, String invoiceColor) {
        boolean isValid = true;

        List<String> allowedApplyStatuses = lovAdapter.queryLovValue(InvoiceApplyConstants.INV_APPLY_HEADER_APPLY_STATUS, BaseConstants.DEFAULT_TENANT_ID)
                .stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        List<String> allowedInvoiceColor = lovAdapter.queryLovValue(InvoiceApplyConstants.INV_APPLY_HEADER_INV_COLOR, BaseConstants.DEFAULT_TENANT_ID)
                .stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        List<String> allowedInvoiceType = lovAdapter.queryLovValue(InvoiceApplyConstants.INV_APPLY_HEADER_INV_TYPE, BaseConstants.DEFAULT_TENANT_ID)
                .stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        if (!allowedApplyStatuses.contains(applyStatus)) {
            isValid = false;
        }

        if (!allowedInvoiceType.contains(invoiceType)) {
            isValid = false;
        }

        if(!allowedInvoiceColor.contains(invoiceColor)) {
            isValid = false;
        }

        return isValid;
    }

}
