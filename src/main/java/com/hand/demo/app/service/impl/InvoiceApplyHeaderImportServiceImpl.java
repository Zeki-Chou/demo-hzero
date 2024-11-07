package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.InvHeaderConstant;
import com.hand.demo.infra.constant.TaskConstant;
import io.choerodon.core.exception.CommonException;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.mybatis.domian.Condition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ImportService(templateCode = "EXAM-47358-HEADER", sheetName = "INVOICE_APPLY_HEADER")
public class InvoiceApplyHeaderImportServiceImpl extends BatchImportHandler {
    @Autowired
    InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    LovAdapter lovAdapter;

    @Autowired
    private CodeRuleBuilder codeRuleBuilder;


    @Override
    public Boolean doImport(List<String> data) {
        try {
            List<InvoiceApplyHeader> invoiceApplyHeaders = parseDataToInvoiceApplyHeaders(data);
            validationOfHeader(invoiceApplyHeaders);

            List<InvoiceApplyHeader> insertList = filterInsertList(invoiceApplyHeaders);
            List<InvoiceApplyHeader> updateList = filterUpdateList(invoiceApplyHeaders);

            processInsertList(insertList);
            processUpdateList(updateList);

            return true;
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    private List<InvoiceApplyHeader> parseDataToInvoiceApplyHeaders(List<String> data) {
        List<InvoiceApplyHeader> invoiceApplyHeaders = new ArrayList<>();
        for (String header : data) {
            if (!header.isEmpty()) {
                try {
                    InvoiceApplyHeader invoiceApplyHeader = objectMapper.readValue(header, InvoiceApplyHeader.class);
                    invoiceApplyHeaders.add(invoiceApplyHeader);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return invoiceApplyHeaders;
    }

    private List<InvoiceApplyHeader> filterInsertList(List<InvoiceApplyHeader> headers) {
        return headers.stream()
                .filter(header -> header.getApplyHeaderNumber() == null)
                .collect(Collectors.toList());
    }

    private List<InvoiceApplyHeader> filterUpdateList(List<InvoiceApplyHeader> headers) {
        return headers.stream()
                .filter(header -> header.getApplyHeaderNumber() != null)
                .collect(Collectors.toList());
    }

    private void processInsertList(List<InvoiceApplyHeader> insertList) {
        if (insertList.isEmpty()) return;

        Map<String, String> variableMap = initializeVariableMap();
        for (InvoiceApplyHeader header : insertList) {
            initializeHeaderAmounts(header);
            header.setTenantId(0L);
            setApplyHeaderNumberIfAbsent(header, variableMap);
            invoiceApplyHeaderRepository.insertSelective(header);
        }
    }

    private Map<String, String> initializeVariableMap() {
        Map<String, String> variableMap = new HashMap<>();
        variableMap.put("customSegment", "-");
        return variableMap;
    }

    private void initializeHeaderAmounts(InvoiceApplyHeader header) {
        header.setTotalAmount(BigDecimal.valueOf(0));
        header.setExcludeTaxAmount(BigDecimal.valueOf(0));
        header.setTaxAmount(BigDecimal.valueOf(0));
    }

    private void setApplyHeaderNumberIfAbsent(InvoiceApplyHeader header, Map<String, String> variableMap) {
        if (header.getApplyHeaderNumber() == null) {
            String batchCodes = codeRuleBuilder.generateCode(InvHeaderConstant.RULE_CODE, variableMap);
            header.setApplyHeaderNumber(batchCodes);
        }
    }

    private void processUpdateList(List<InvoiceApplyHeader> updateList) {
        if (updateList.isEmpty()) return;

        for (InvoiceApplyHeader updateHeader : updateList) {
            updateHeaderPrimaryKeyAndVersion(updateHeader);
        }
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateList);
    }

    private void updateHeaderPrimaryKeyAndVersion(InvoiceApplyHeader updateHeader) {
        Condition condition = createHeaderCondition(updateHeader.getApplyHeaderNumber());
        List<InvoiceApplyHeader> headerData = invoiceApplyHeaderRepository.selectByCondition(condition);

        if (!headerData.isEmpty()) {
            InvoiceApplyHeader existingHeader = headerData.get(0);
            updateHeader.setApplyHeaderId(existingHeader.getApplyHeaderId());
            updateHeader.setObjectVersionNumber(existingHeader.getObjectVersionNumber());
        }
    }

    private Condition createHeaderCondition(String applyHeaderNumber) {
        Condition condition = new Condition(InvoiceApplyHeader.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.orEqualTo(InvoiceApplyHeader.FIELD_APPLY_HEADER_NUMBER, applyHeaderNumber);
        return condition;
    }


    private void validationOfHeader(List<InvoiceApplyHeader> invoiceApplyHeaders) {
        List<LovValueDTO> validApplyTypesList = lovAdapter.queryLovValue(InvHeaderConstant.APPLY_TYPE_CODE,
                Long.valueOf(TaskConstant.TENANT_ID));
        List<LovValueDTO> validColorTypesList = lovAdapter.queryLovValue(InvHeaderConstant.INVOICE_COLOR_CODE,
                Long.valueOf(TaskConstant.TENANT_ID));
        List<LovValueDTO> validStatusTypesList = lovAdapter.queryLovValue(InvHeaderConstant.APPLY_STATUS_CODE,
                Long.valueOf(TaskConstant.TENANT_ID));

        List<String> validColorTypes = validColorTypesList.stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        List<String> validApplyTypes = validApplyTypesList.stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        List<String> validStatusTypes = validStatusTypesList.stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        StringBuilder errorMessages = new StringBuilder();
        for (int i = 0; i < invoiceApplyHeaders.size(); i++) {
            InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaders.get(i);
            StringBuilder lineError = new StringBuilder();

            if (!validColorTypes.contains(invoiceApplyHeader.getInvoiceColor())) {
                lineError.append("Invoice color: ").append(invoiceApplyHeader.getInvoiceColor()).append(", ");
            }
            if (!validApplyTypes.contains(invoiceApplyHeader.getInvoiceType())) {
                lineError.append("Invoice Type: ").append(invoiceApplyHeader.getInvoiceType()).append(", ");
            }
            if (!validStatusTypes.contains(invoiceApplyHeader.getApplyStatus())) {
                lineError.append("Apply status: ").append(invoiceApplyHeader.getApplyStatus()).append(", ");
            }

            if (lineError.length() > 0) {
                errorMessages.append("line num: ").append(i + 1).append(", ").append(lineError).append("\n");
            }
        }

        String finalErrorMessages = errorMessages.toString();
        if (errorMessages.length() > 0) {
            throw new CommonException(finalErrorMessages);
        }
    }
}
