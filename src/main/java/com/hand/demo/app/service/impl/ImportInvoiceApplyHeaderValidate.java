package com.hand.demo.app.service.impl;

import com.hand.demo.domain.entity.InvCountHeader;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.Constants;
import io.choerodon.core.exception.CommonException;
import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.domain.entity.ImportData;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import org.hzero.mybatis.domian.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@ImportValidators({
        @ImportValidator(templateCode = "INVOICE-APPLY-HEADER-47360", sheetName = "Header",sheetIndex = 0)
})
public class ImportInvoiceApplyHeaderValidate extends BatchValidatorHandler {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LovAdapter lovAdapter;
    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;


    @Override
    public boolean validate(List<String> data) {
        List<InvoiceApplyHeader> invoiceApplyHeaders = new ArrayList<>();
        for (String singleData:data) {
            try {
                InvoiceApplyHeader invoiceApplyHeader = objectMapper.readValue(singleData, InvoiceApplyHeader.class);
                invoiceApplyHeaders.add(invoiceApplyHeader);
            } catch (Exception e) {
                return false;
            }
        }

        validateLovValue(invoiceApplyHeaders,getContext());
        validateHeaderExistence(invoiceApplyHeaders,getContext());

        return getContext().stream().noneMatch(ctx -> ctx.getErrorMsg() != null && !ctx.getErrorMsg().isEmpty());
    }

    public void validateLovValue(List<InvoiceApplyHeader> invoiceApplyHeaders, List<ImportData> importDataCtx){
        List<LovValueDTO> applyStatusLov = lovAdapter.queryLovValue(Constants.LOV_INV_APPLY_HEADER_APPLY_STATUS, BaseConstants.DEFAULT_TENANT_ID);
        List<LovValueDTO> invTypeLov = lovAdapter.queryLovValue(Constants.LOV_INV_APPLY_HEADER_INV_TYPE, BaseConstants.DEFAULT_TENANT_ID);
        List<LovValueDTO> invColorLov = lovAdapter.queryLovValue(Constants.LOV_INV_APPLY_HEADER_INV_COLOR, BaseConstants.DEFAULT_TENANT_ID);

        List<String> applyStatuses = applyStatusLov.stream().map(LovValueDTO::getValue).collect(Collectors.toList());
        List<String> invTypes = invTypeLov.stream().map(LovValueDTO::getValue).collect(Collectors.toList());
        List<String> invColors = invColorLov.stream().map(LovValueDTO::getValue).collect(Collectors.toList());

        for (int i = 0; i<invoiceApplyHeaders.size(); i++){
            if(!applyStatuses.contains(invoiceApplyHeaders.get(i).getApplyStatus())
            || !invTypes.contains(invoiceApplyHeaders.get(i).getInvoiceType())
            || !invColors.contains(invoiceApplyHeaders.get(i).getInvoiceColor())){
                importDataCtx.get(i).setErrorMsg("Fail Lov Validation");
            }
        }

    }

    public void validateHeaderExistence(List<InvoiceApplyHeader> invoiceApplyHeaders, List<ImportData> importDataCtx){
        Set<String> invoiceHeaderNumbers = invoiceApplyHeaders.stream().map(InvoiceApplyHeader::getApplyHeaderNumber).filter(Objects::nonNull).collect(Collectors.toSet());
        if(invoiceHeaderNumbers.isEmpty()){
            return;
        }

        Condition condition = new Condition(InvoiceApplyHeader.class);
        Condition.Criteria criteria = condition.createCriteria();
        for (String invoiceHeaderNumber:invoiceHeaderNumbers){
            criteria.orEqualTo(InvoiceApplyHeader.FIELD_APPLY_HEADER_NUMBER, invoiceHeaderNumber);
        }
        Set<String> foundInvoiceHeaderNumbers =  invoiceApplyHeaderRepository.selectByCondition(condition).stream().map(InvoiceApplyHeader::getApplyHeaderNumber).collect(Collectors.toSet());

        for (int i=0;i<invoiceApplyHeaders.size();i++){
            if(invoiceApplyHeaders.get(i).getApplyHeaderNumber()!=null && !foundInvoiceHeaderNumbers.contains(invoiceApplyHeaders.get(i).getApplyHeaderNumber())){
                importDataCtx.get(i).setErrorMsg("Fail Update Exist Validation");
            }
        }
    }
}
