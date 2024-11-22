package com.hand.demo.app.service.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.BaseConstant;
import com.hand.demo.infra.constant.InvApplyHeaderConstant;
import com.hand.demo.infra.util.Utils;
import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;

import javax.rmi.CORBA.Util;
import java.util.List;
import java.util.stream.Collectors;

@ImportValidators({
        @ImportValidator(templateCode = InvApplyHeaderConstant.TEMPLATE_CODE)
})
public class InvoiceApplyHeaderValidator extends BatchValidatorHandler {

    private final ObjectMapper objectMapper;
    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    private final LovAdapter lovAdapter;

    public InvoiceApplyHeaderValidator(ObjectMapper objectMapper, InvoiceApplyHeaderRepository invoiceApplyHeaderRepository, LovAdapter lovAdapter) {
        this.objectMapper = objectMapper;
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
        this.lovAdapter = lovAdapter;
    }

    @Override
    public boolean validate(List<String> data) {
        Long organizationId = 0L;
        List<String> applyStatusList = Utils.getListLovValues(lovAdapter, BaseConstant.InvApplyHeader.APPLY_STATUS_CODE, organizationId);
        List<String> invoiceColorList = Utils.getListLovValues(lovAdapter, BaseConstant.InvApplyHeader.INVOICE_COLOR_CODE, organizationId);
        List<String> invoiceTypeList = Utils.getListLovValues(lovAdapter, BaseConstant.InvApplyHeader.INVOICE_TYPE_CODE, organizationId);

        boolean flag = true;
        for (int i = 0; i < data.size(); i++) {
            try {
                InvoiceApplyHeader header = objectMapper.readValue(data.get(i), InvoiceApplyHeader.class);

                if (!invoiceTypeList.contains(header.getInvoiceType())) {
                    getContext().get(i).addErrorMsg("invalid invoice type");
                    flag = false;
                }

                if (!invoiceColorList.contains(header.getInvoiceColor())) {
                    getContext().get(i).addErrorMsg("invalid invoice color");
                    flag = false;
                }

                if (!applyStatusList.contains(header.getApplyStatus())) {
                    getContext().get(i).addErrorMsg("invalid apply status");
                    flag = false;
                }

                // check whether header number exists in database when updating header
                if (header.getApplyHeaderNumber() != null) {
                    InvoiceApplyHeader headerRecord = new InvoiceApplyHeader();
                    headerRecord.setApplyHeaderNumber(header.getApplyHeaderNumber());
                    InvoiceApplyHeader exist = invoiceApplyHeaderRepository.selectOne(headerRecord);
                    if (exist == null) {
                        getContext().get(i).addErrorMsg("header number does not exist in database");
                        flag = false;
                    }
                }

            } catch (JsonProcessingException e) {
                getContext().get(i).addErrorMsg("Error reading JSON context");
                flag = false;
            }
        }

        return flag;
    }
}
