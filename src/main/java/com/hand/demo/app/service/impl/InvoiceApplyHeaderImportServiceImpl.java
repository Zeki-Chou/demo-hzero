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
            List<InvoiceApplyHeader> invoiceApplyHeaders = new ArrayList<>();

            for (String header : data) {
                if (!header.isEmpty() || header.length() != 0) {
                    try {
                        InvoiceApplyHeader invoiceApplyHeader = objectMapper.readValue(header, InvoiceApplyHeader.class);
                        invoiceApplyHeaders.add(invoiceApplyHeader);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            validationOfHeader(invoiceApplyHeaders);


            List<InvoiceApplyHeader> insertList = invoiceApplyHeaders.stream()
                    .filter(header -> header.getApplyHeaderNumber() == null)
                    .collect(Collectors.toList());

            List<InvoiceApplyHeader> updateList = invoiceApplyHeaders.stream()
                    .filter(header -> header.getApplyHeaderNumber() != null)
                    .collect(Collectors.toList());

            if (!insertList.isEmpty()) {
                Map<String, String> variableMap = new HashMap<>();
                variableMap.put("customSegment", "-");
                for (InvoiceApplyHeader header : insertList) {
                    header.setTotalAmount(BigDecimal.valueOf(0));
                    header.setExcludeTaxAmount(BigDecimal.valueOf(0));
                    header.setTaxAmount(BigDecimal.valueOf(0));
                    header.setTenantId(0L);
                    if (header.getApplyHeaderNumber() == null) {
                        String batchCodes = codeRuleBuilder.generateCode(InvHeaderConstant.RULE_CODE, variableMap);
                        header.setApplyHeaderNumber(batchCodes);
                    }
                    invoiceApplyHeaderRepository.insertSelective(header);
                }
            }


            if (!updateList.isEmpty()) {
                for (InvoiceApplyHeader updateHeader: updateList) {
                    Condition condition = new Condition(InvoiceApplyHeader.class);
                    Condition.Criteria criteria = condition.createCriteria();
                    criteria.orEqualTo(InvoiceApplyHeader.FIELD_APPLY_HEADER_NUMBER, updateHeader.getApplyHeaderNumber());
                    List<InvoiceApplyHeader> headerDatas = invoiceApplyHeaderRepository.selectByCondition(condition);
                    updateHeader.setApplyHeaderId(headerDatas.get(0).getApplyHeaderId());
                    updateHeader.setObjectVersionNumber(headerDatas.get(0).getObjectVersionNumber());
                }
                invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateList);
            }
            return true;
        } catch (Exception e) {
            throw new CommonException(e);
        }
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
