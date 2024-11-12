package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.InvoiceConstants;
import io.choerodon.core.exception.CommonException;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import org.hzero.mybatis.domian.Condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ImportService(templateCode = "EXAM-47357-HEADER", sheetName = "INV_APPLY_HEADER")
public class InvoiceApplyHeaderImportServiceImpl extends BatchImportHandler {

    private final ObjectMapper objectMapper;
    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    private final LovAdapter lovAdapter;
    private final CodeRuleBuilder codeRuleBuilder;

    public InvoiceApplyHeaderImportServiceImpl(ObjectMapper objectMapper,
                                               InvoiceApplyHeaderRepository invoiceApplyHeaderRepository,
                                               LovAdapter lovAdapter,
                                               CodeRuleBuilder codeRuleBuilder) {
        this.objectMapper = objectMapper;
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
        this.lovAdapter = lovAdapter;
        this.codeRuleBuilder = codeRuleBuilder;
    }

    @Override
    public Boolean doImport(List<String> data) {
        try {
            List<InvoiceApplyHeader> headersToInsert = new ArrayList<>();
            List<InvoiceApplyHeader> headersToUpdate = new ArrayList<>();

            // Fetch valid LOV values for validation
            List<String> validApplyStatuses = lovAdapter.queryLovValue(InvoiceConstants.LovCode.APPLY_STATUS, BaseConstants.DEFAULT_TENANT_ID)
                    .stream().map(LovValueDTO::getValue).collect(Collectors.toList());
            List<String> validInvoiceColors = lovAdapter.queryLovValue(InvoiceConstants.LovCode.INVOICE_COLOR, BaseConstants.DEFAULT_TENANT_ID)
                    .stream().map(LovValueDTO::getValue).collect(Collectors.toList());
            List<String> validInvoiceTypes = lovAdapter.queryLovValue(InvoiceConstants.LovCode.INVOICE_TYPE, BaseConstants.DEFAULT_TENANT_ID)
                    .stream().map(LovValueDTO::getValue).collect(Collectors.toList());

            List<String> invalidHeaders = new ArrayList<>();

            // Parse, validate, and categorize for insert or update
            for (String jsonData : data) {
                InvoiceApplyHeader header = objectMapper.readValue(jsonData, InvoiceApplyHeader.class);

                // Validate fields against LOV values
                if (!validApplyStatuses.contains(header.getApplyStatus())) {
                    invalidHeaders.add("Apply Status: " + header.getApplyStatus() + " is invalid.");
                }
                if (!validInvoiceColors.contains(header.getInvoiceColor())) {
                    invalidHeaders.add("Invoice Color: " + header.getInvoiceColor() + " is invalid.");
                }
                if (!validInvoiceTypes.contains(header.getInvoiceType())) {
                    invalidHeaders.add("Invoice Type: " + header.getInvoiceType() + " is invalid.");
                }

                if (invalidHeaders.isEmpty()) {
                    // Determine if the header is new or existing
                    if (header.getApplyHeaderNumber() == null || header.getApplyHeaderNumber().isEmpty()) {
                        // Generate a unique code for new headers
                        Map<String, String> variableMap = new HashMap<>();
                        variableMap.put("customSegment", "-");
                        String uniqueCode = codeRuleBuilder.generateCode(InvoiceConstants.CODE_RULE, variableMap);
                        header.setApplyHeaderNumber(uniqueCode);
                        header.setTenantId(0L);

                        headersToInsert.add(header);
                    } else {
                        // Use Condition and Criteria to fetch the existing header based on applyHeaderNumber
                        Condition condition = new Condition(InvoiceApplyHeader.class);
                        condition.createCriteria().andEqualTo("applyHeaderNumber", header.getApplyHeaderNumber());

                        List<InvoiceApplyHeader> existingHeaders = invoiceApplyHeaderRepository.selectByCondition(condition);

                        if (!existingHeaders.isEmpty()) {
                            InvoiceApplyHeader existingHeader = existingHeaders.get(0);
                            header.setApplyHeaderId(existingHeader.getApplyHeaderId());
                            headersToUpdate.add(header);
                        } else {
                            throw new CommonException(InvoiceConstants.Exception.INVALID_APPLY_HEADER, header.getApplyHeaderNumber());
                        }
                    }
                }
            }

            if (!invalidHeaders.isEmpty()) {
                throw new CommonException(InvoiceConstants.Exception.INVALID_APPLY_HEADER, String.join(", ", invalidHeaders));
            }

            // Batch insert new headers
            if (!headersToInsert.isEmpty()) {
                invoiceApplyHeaderRepository.batchInsertSelective(headersToInsert);
            }

            // Batch update existing headers
            if (!headersToUpdate.isEmpty()) {
                // Perform the update operation
                invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(headersToUpdate);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
