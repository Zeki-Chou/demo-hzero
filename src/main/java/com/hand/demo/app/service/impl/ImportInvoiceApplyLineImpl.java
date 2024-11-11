package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.Constants;
import com.hand.demo.infra.util.Utils;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.hzero.core.redis.RedisHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ImportService(templateCode = "INVOICE-APPLY-HEADER-47360",sheetName = "Line",sheetIndex = 1)
public class ImportInvoiceApplyLineImpl extends BatchImportHandler {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;
    @Autowired
    private RedisHelper redisHelper;

    @Override
    public Boolean doImport(List<String> data) {
        List<InvoiceApplyLine> invoiceApplyLines = new ArrayList<>();
        for (String singleData : data) {
            try {
                InvoiceApplyLine invoiceApplyLine = objectMapper.readValue(singleData, InvoiceApplyLine.class);
                invoiceApplyLines.add(invoiceApplyLine);
            } catch (Exception e) {
                return false;
            }
        }

        Utils.InvoiceApplyLineUtil.calcAmounts(invoiceApplyLines);
        insertLine(invoiceApplyLines.stream().filter(line -> line.getApplyLineId()==null).collect(Collectors.toList()));
        updateLine(invoiceApplyLines.stream().filter(line -> line.getApplyLineId()!=null).collect(Collectors.toList()));
        invalidateHeaderCache(invoiceApplyLines);

        return true;
    }

    private void insertLine(List<InvoiceApplyLine> invoiceApplyLines){
        if(invoiceApplyLines.isEmpty()) {
            return;
        }

        String headerIds = invoiceApplyLines.stream().map(line->line.getApplyHeaderId().toString()).collect(Collectors.joining(","));
        List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceApplyHeaderRepository.selectByIds(headerIds);
        Utils.InvoiceApplyHeaderUtil.addAmounts(invoiceApplyHeaders,invoiceApplyLines);
        invoiceApplyLineRepository.batchInsertSelective(invoiceApplyLines);
        invoiceApplyHeaderRepository.batchUpdateOptional(invoiceApplyHeaders,InvoiceApplyHeader.FIELD_TOTAL_AMOUNT,InvoiceApplyHeader.FIELD_EXCLUDE_TAX_AMOUNT,InvoiceApplyHeader.FIELD_TAX_AMOUNT);
    }

    private  void updateLine(List<InvoiceApplyLine> invoiceApplyLines){
        if(invoiceApplyLines.isEmpty()) {
            return;
        }

        String headerIds = invoiceApplyLines.stream().map(line->line.getApplyHeaderId().toString()).collect(Collectors.joining(","));
        String lineIds = invoiceApplyLines.stream().map(line->line.getApplyLineId().toString()).collect(Collectors.joining(","));
        List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceApplyHeaderRepository.selectByIds(headerIds);
        List<InvoiceApplyLine> oldInvoiceApplyLines = invoiceApplyLineRepository.selectByIds(lineIds);
        Utils.InvoiceApplyHeaderUtil.updateAmounts(invoiceApplyHeaders,invoiceApplyLines,oldInvoiceApplyLines);
        for(int i=0;i<invoiceApplyLines.size();i++){
            invoiceApplyLines.get(i).setObjectVersionNumber(oldInvoiceApplyLines.get(i).getObjectVersionNumber());
        }
        invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(invoiceApplyLines);
        invoiceApplyHeaderRepository.batchUpdateOptional(invoiceApplyHeaders,InvoiceApplyHeader.FIELD_TOTAL_AMOUNT,InvoiceApplyHeader.FIELD_EXCLUDE_TAX_AMOUNT,InvoiceApplyHeader.FIELD_TAX_AMOUNT);
    }

    private void invalidateHeaderCache(List<InvoiceApplyLine> invoiceApplyLines){
        if(invoiceApplyLines.isEmpty()) {
            return;
        }

        Set<String> redisKeys = invoiceApplyLines.stream().map(line->Constants.INVOICE_HEADER_CACHE_PREFIX +line.getApplyHeaderId().toString()).collect(Collectors.toSet());
        redisHelper.delKeys(redisKeys);
    }
}