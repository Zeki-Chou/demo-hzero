package com.hand.demo.app.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.InvoiceApplyHeaderConstant;
import lombok.AllArgsConstructor;
import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;

import java.util.List;
import java.util.stream.Collectors;



@ImportValidators({
        @ImportValidator(templateCode = "EXAM-47361-HEADER", sheetName = "Invoice_Apply_Header")
})
@AllArgsConstructor
public class InvoiceApplyHeaderImportValidator extends BatchValidatorHandler {
    private ObjectMapper objectMapper;
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    private LovAdapter lovAdapter;

    @Override
    public boolean validate(List<String> data) {
        List<String> validApplyStatus = getValidLovValues(InvoiceApplyHeaderConstant.LovCode.APPLY_STATUS);
        List<String> validInvoiceType = getValidLovValues(InvoiceApplyHeaderConstant.LovCode.INVOICE_TYPE);
        List<String> validInvoiceColor = getValidLovValues(InvoiceApplyHeaderConstant.LovCode.INVOICE_COLOR);

        boolean flag = true;
        if(CollUtil.isNotEmpty(data)){
            for(int i = 0; i < data.size(); i++){
                try {
                    InvoiceApplyHeader header = objectMapper.readValue(data.get(i), InvoiceApplyHeader.class);
                    validateInvoiceHeader(flag, i, header, validApplyStatus, validInvoiceType, validInvoiceColor);
                }catch (Exception exception){
                    addErrorMsg(i, exception.getMessage());
                    flag = false;
                }
            }
        }
        return flag;
    }

    private List<String> getValidLovValues(String lovCode) {
        return lovAdapter
                .queryLovValue(lovCode, BaseConstants.DEFAULT_TENANT_ID)
                .stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());
    }

    private void validateInvoiceHeader(boolean flag, int index, InvoiceApplyHeader invoiceHeader, List<String> validApplyStatus,
                                    List<String> validInvoiceType, List<String> validInvoiceColor) {
        if (!validApplyStatus.contains(invoiceHeader.getApplyStatus())) {
            addErrorMsg(index,"Invoice Status Invalid");
            flag = false;
        }
        if (!validInvoiceType.contains(invoiceHeader.getInvoiceType())) {
            addErrorMsg(index,"Invoice Type Invalid");
            flag = false;
        }
        if (!validInvoiceColor.contains(invoiceHeader.getInvoiceColor())) {
            addErrorMsg(index,"Invoice Color Invalid");
            flag = false;
        }
    }

}
