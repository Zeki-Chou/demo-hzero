package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.Constants;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.core.redis.RedisHelper;
import org.hzero.mybatis.domian.Condition;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@ImportService(templateCode = "INVOICE-APPLY-HEADER-47360",sheetName = "Header",sheetIndex = 0)
public class ImportInvoiceApplyHeaderImpl extends BatchImportHandler {
    @Autowired
    private  ObjectMapper objectMapper;
    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    @Autowired
    private CodeRuleBuilder codeRuleBuilder;
    @Autowired
    private RedisHelper redisHelper;

    @Override
    public Boolean doImport(List<String> data) {
        List<InvoiceApplyHeader> invoiceApplyHeaders = new ArrayList<>();
        for (String singleData:data) {
            try {
                InvoiceApplyHeader invoiceApplyHeader = objectMapper.readValue(singleData, InvoiceApplyHeader.class);
                invoiceApplyHeaders.add(invoiceApplyHeader);
            } catch (Exception e) {
                return false;
            }
        }

        insertHeaders(invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderNumber() == null).collect(Collectors.toList()));
        updateHeaders(invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderNumber() != null).collect(Collectors.toList()));

        return true;
    }

    private void insertHeaders(List<InvoiceApplyHeader> invoiceApplyHeaders){
        if(invoiceApplyHeaders.isEmpty()){
            return;
        }

        List<String> applyHeaderNumbers = codeRuleBuilder.generateCode(invoiceApplyHeaders.size(), Constants.CODERULE_INV_APPLY_HEADER,null);
        for(int i=0;i<invoiceApplyHeaders.size();i++){
            invoiceApplyHeaders.get(i).setApplyHeaderNumber(applyHeaderNumbers.get(i));
            invoiceApplyHeaders.get(i).setDelFlag(0);
            invoiceApplyHeaders.get(i).setTotalAmount(new BigDecimal(0));
            invoiceApplyHeaders.get(i).setExcludeTaxAmount(new BigDecimal(0));
            invoiceApplyHeaders.get(i).setTaxAmount(new BigDecimal(0));
            invoiceApplyHeaders.get(i).setTenantId(0L);
        }
        invoiceApplyHeaderRepository.batchInsertSelective(invoiceApplyHeaders);
    }

    private void updateHeaders(List<InvoiceApplyHeader> invoiceApplyHeaders){
        if(invoiceApplyHeaders.isEmpty()){
            return;
        }

        Map<String, InvoiceApplyHeader> invoiceApplyHeaderMap = new HashMap<>();
        Condition condition = new Condition(InvoiceApplyHeader.class);
        Condition.Criteria criteria = condition.createCriteria();
        for(InvoiceApplyHeader updateHeader:invoiceApplyHeaders){
            invoiceApplyHeaderMap.put(updateHeader.getApplyHeaderNumber(),updateHeader);
            criteria.orEqualTo(InvoiceApplyHeader.FIELD_APPLY_HEADER_NUMBER,updateHeader.getApplyHeaderNumber());
        }
        List<InvoiceApplyHeader> oldInvoiceApplyHeaders = invoiceApplyHeaderRepository.selectByCondition(condition);

        for(InvoiceApplyHeader oldInvoiceApplyHeader:oldInvoiceApplyHeaders){
            InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderMap.get(oldInvoiceApplyHeader.getApplyHeaderNumber());

            invoiceApplyHeader.setObjectVersionNumber(oldInvoiceApplyHeader.getObjectVersionNumber());
            invoiceApplyHeader.setApplyHeaderId(oldInvoiceApplyHeader.getApplyHeaderId());
        }

        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(invoiceApplyHeaders);

        Set<String> redisKeys = invoiceApplyHeaders.stream().map(header->Constants.INVOICE_HEADER_CACHE_PREFIX +header.getApplyHeaderId().toString()).collect(Collectors.toSet());
        redisHelper.delKeys(redisKeys);
    }
}
