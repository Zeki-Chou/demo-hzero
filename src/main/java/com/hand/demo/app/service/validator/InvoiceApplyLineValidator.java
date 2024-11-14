package com.hand.demo.app.service.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.InvApplyHeaderConstant;
import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;

import java.util.List;

@ImportValidators({
        @ImportValidator(templateCode = InvApplyHeaderConstant.TEMPLATE_CODE, sheetIndex = 1)
})
public class InvoiceApplyLineValidator extends BatchValidatorHandler {
    private final ObjectMapper objectMapper;
    private final InvoiceApplyLineRepository invoiceApplyLineRepository;
    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    public InvoiceApplyLineValidator(ObjectMapper objectMapper, InvoiceApplyLineRepository invoiceApplyLineRepository, InvoiceApplyHeaderRepository invoiceApplyHeaderRepository) {
        this.objectMapper = objectMapper;
        this.invoiceApplyLineRepository = invoiceApplyLineRepository;
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
    }

    @Override
    public boolean validate(List<String> data) {
        boolean flag = true;

        for (int i = 0; i < data.size(); i++) {
            try {
                InvoiceApplyLine invoiceApplyLine = objectMapper.readValue(data.get(i), InvoiceApplyLine.class);

                InvoiceApplyHeader findHeader = invoiceApplyHeaderRepository.selectByPrimary(
                        invoiceApplyLine.getApplyHeaderId()
                );

                if (findHeader == null) {
                    getContext().get(i).addErrorMsg("invoice apply header not found");
                    flag = false;
                } else if (findHeader.getDelFlag() == 1) {
                    getContext().get(i).addErrorMsg("invoice apply header has been deleted");
                    flag = false;
                }

                if (invoiceApplyLine.getApplyLineId() != null) {
                    // if update invoice apply line, then check exist in database
                    InvoiceApplyLine findApplyLine = invoiceApplyLineRepository.selectByPrimary(invoiceApplyLine.getApplyLineId());
                    if (findApplyLine == null) {
                        getContext().get(i).addErrorMsg("invoice apply line not found in database");
                        flag = false;
                    }
                }

            } catch (JsonProcessingException e) {
                getContext().get(i).addErrorMsg("Error processing JSON context");
                flag = false;
            }
        }

        return flag;
    }
}
