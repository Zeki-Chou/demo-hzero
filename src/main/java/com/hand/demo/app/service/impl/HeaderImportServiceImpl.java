package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.InvoiceApplyHeaderConstant;

import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

//sheetIndex;

@ImportValidators({
        @ImportValidator(templateCode = "EXAM-47356-HEADER")
})
public class HeaderImportServiceImpl extends BatchValidatorHandler {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    @Autowired
    private LovAdapter lovAdapter;

    @Override
    public boolean validate(List<String> data) {
        AtomicBoolean flag = new AtomicBoolean(true);

        if (data == null || data.isEmpty()) {
            return Boolean.FALSE;
        }

//        validate errorfirst
        List<LovValueDTO> countInvoiceType = lovAdapter.queryLovValue(InvoiceApplyHeaderConstant.INVOICE_TYPE, BaseConstants.DEFAULT_TENANT_ID);
        List<LovValueDTO> countInvoiceColor = lovAdapter.queryLovValue(InvoiceApplyHeaderConstant.INVOICE_COLOR, BaseConstants.DEFAULT_TENANT_ID);
        List<LovValueDTO> countApplyStatus = lovAdapter.queryLovValue(InvoiceApplyHeaderConstant.APPLY_STATUS, BaseConstants.DEFAULT_TENANT_ID);

        List<String> invoiceType = countInvoiceType.stream().map(LovValueDTO::getValue).collect(Collectors.toList());
        List<String> invoiceColor = countInvoiceColor.stream().map(LovValueDTO::getValue).collect(Collectors.toList());
        List<String> applyStatus = countApplyStatus.stream().map(LovValueDTO::getValue).collect(Collectors.toList());

        AtomicInteger index = new AtomicInteger(0);
        data.forEach(invoiceApplyHeader -> {
            List<String> validationError = new ArrayList<>();

            int i = index.getAndIncrement();
            try {
                InvoiceApplyHeader invoiceApplyHeaderObj = objectMapper.readValue(invoiceApplyHeader, InvoiceApplyHeader.class);

                if(!invoiceType.contains(invoiceApplyHeaderObj.getInvoiceType())) {
                    validationError.add("Error Invoice Type : " + invoiceApplyHeaderObj.getInvoiceType());
                }

                if(!invoiceColor.contains(invoiceApplyHeaderObj.getInvoiceColor())) {
                    validationError.add("Error Invoice Color : " + invoiceApplyHeaderObj.getInvoiceColor());
                }

                if(!applyStatus.contains(invoiceApplyHeaderObj.getApplyStatus())) {
                    validationError.add("Error Apply Status : " + invoiceApplyHeaderObj.getApplyStatus());
                }

            } catch (JsonProcessingException e) {
                getContext().get(i).addErrorMsg("Fail to read data");
                flag.set(false);
            }

            if (!validationError.isEmpty()) {
                getContext().get(i).addErrorMsg(validationError.toString());
                flag.set(false);
            }
        });

//        if validate is true
        if (flag.get()) {
            List<InvoiceApplyHeader> headerListInsert = new ArrayList<>();
            List<InvoiceApplyHeader> headerListUpdate = new ArrayList<>();

            AtomicInteger index2 = new AtomicInteger(0);
            data.forEach(invoiceApplyHeader -> {
                int i = index2.getAndIncrement();
                try {
                    InvoiceApplyHeader invoiceApplyHeaderObj = objectMapper.readValue(invoiceApplyHeader, InvoiceApplyHeader.class);
                    String applyHeaderNumber = invoiceApplyHeaderObj.getApplyHeaderNumber();

                    InvoiceApplyHeader invoiceApplyHeaderNew = new InvoiceApplyHeader();
                    invoiceApplyHeaderNew.setApplyHeaderNumber(applyHeaderNumber);

//                check database
                    List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceApplyHeaderRepository.select(invoiceApplyHeaderNew);

                    if(invoiceApplyHeaders.size() > 0) {
                        InvoiceApplyHeader invoiceApplyHeader1 = invoiceApplyHeaders.get(0);

                        invoiceApplyHeaderObj.setApplyHeaderId(invoiceApplyHeader1.getApplyHeaderId());
                        invoiceApplyHeaderObj.setObjectVersionNumber(invoiceApplyHeader1.getObjectVersionNumber());
                        headerListUpdate.add(invoiceApplyHeaderObj);
                    } else {
                        invoiceApplyHeaderObj.setTaxAmount(BigDecimal.ZERO);
                        invoiceApplyHeaderObj.setTotalAmount(BigDecimal.ZERO);
                        invoiceApplyHeaderObj.setExcludeTaxAmount(BigDecimal.ZERO);

                        invoiceApplyHeaderObj.setTenantId(0L);
                        invoiceApplyHeaderObj.setDelFlag(0);
                        headerListInsert.add(invoiceApplyHeaderObj);
                    }
                } catch (JsonProcessingException e) {
                    getContext().get(i).addErrorMsg("Fail to read data");
                    flag.set(false);
                }
            });

            invoiceApplyHeaderRepository.batchInsertSelective(headerListInsert);
            invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(headerListUpdate);
        }
        return flag.get();
    }
}
