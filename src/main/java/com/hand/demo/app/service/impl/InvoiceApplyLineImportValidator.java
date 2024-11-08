package com.hand.demo.app.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import lombok.AllArgsConstructor;
import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;
import org.hzero.boot.platform.lov.adapter.LovAdapter;

import java.util.List;

@ImportValidators({
        @ImportValidator(templateCode = "EXAM-47361-HEADER", sheetName = "Invoice_Apply_Line")
})
@AllArgsConstructor
public class InvoiceApplyLineImportValidator extends BatchValidatorHandler {
    private final ObjectMapper objectMapper;
    private final InvoiceApplyLineRepository invoiceApplyLineRepository;
    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    private final LovAdapter lovAdapter;

    @Override
    public boolean validate(List<String> data) {
        boolean flag = true;
        if(CollUtil.isNotEmpty(data)){
            for(int i = 0; i < data.size(); i++){
                try {
                    InvoiceApplyLine line = objectMapper.readValue(data.get(i), InvoiceApplyLine.class);
                    if(line.getApplyHeaderId() == null){
                        addErrorMsg(i, "Invoice Line should have Header Id");
                        flag = false;
                    }
                    InvoiceApplyHeader headerExists = invoiceApplyHeaderRepository.selectByPrimaryKey(line.getApplyHeaderId());
                    if(headerExists == null || headerExists.getDelFlag() == 1){
                        addErrorMsg(i, "Cannot add Line with No Header Exists or has been Deleted");
                        flag = false;
                    }
                }catch (Exception exception){
                    addErrorMsg(i, exception.getMessage());
                    flag = false;
                }
            }
        }
        return flag;
    }
}
