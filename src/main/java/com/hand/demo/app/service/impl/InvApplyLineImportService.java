package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import lombok.AllArgsConstructor;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.hzero.mybatis.domian.Condition;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ImportService(templateCode = "EXAM-47355-APPLY-HEADER", sheetName = "invoice-apply-line")
@AllArgsConstructor
public class InvApplyLineImportService extends BatchImportHandler {
    private ObjectMapper objectMapper;

    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Override
    public Boolean doImport(List<String> data) {
        List<InvoiceApplyLine> invoiceApplyLines = new ArrayList<>();

        for(String json : data) {
            try {
                InvoiceApplyLine line = objectMapper.readValue(json, InvoiceApplyLine.class);
                invoiceApplyLines.add(line);
            } catch (JsonProcessingException e) {
                return Boolean.FALSE;
            }
        }

        List<InvoiceApplyLine> saveApplyLines = invoiceApplyLines.stream()
                .peek(line -> {
                    line.setTotalAmount(line.getUnitPrice().multiply(line.getQuantity()));
                    line.setTaxAmount(line.getTotalAmount().multiply(line.getTaxRate()));
                    line.setExcludeTaxAmount(line.getTotalAmount().subtract(line.getTaxAmount()));
                })
                .collect(Collectors.toList());

        List<InvoiceApplyLine> insertList = saveApplyLines.stream()
                .filter(line -> line.getApplyLineId() == null)
                .collect(Collectors.toList());

        List<InvoiceApplyLine> updateList = saveApplyLines.stream()
                .filter(line -> line.getApplyLineId() != null)
                .collect(Collectors.toList());

        if (!insertList.isEmpty()) {
            invoiceApplyLineRepository.batchInsertSelective(insertList);
        }

        if (!updateList.isEmpty()) {
            invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);
        }

        Set<Long> headerIds = Stream.concat(insertList.stream(), updateList.stream())
                .map(InvoiceApplyLine::getApplyHeaderId)
                .collect(Collectors.toSet());

        Condition applyLineByHeaderIdCondition = new Condition(InvoiceApplyLine.class);
        Condition.Criteria applyLineByHeaderIdCriteria = applyLineByHeaderIdCondition.createCriteria();
        if (!headerIds.isEmpty()) {
            applyLineByHeaderIdCriteria.andIn("applyHeaderId", headerIds);
        } else {
            applyLineByHeaderIdCriteria.andEqualTo("applyHeaderId", -1);
        }
        List<InvoiceApplyLine> allLinesForHeaders = invoiceApplyLineRepository.selectByCondition(applyLineByHeaderIdCondition);

        Map<Long, List<InvoiceApplyLine>> linesGroupedByHeaderId = allLinesForHeaders.stream()
                .collect(Collectors.groupingBy(InvoiceApplyLine::getApplyHeaderId));

        Condition existingHeadersCondition = new Condition(InvoiceApplyLine.class);
        Condition.Criteria existingHeaderCriteria = existingHeadersCondition.createCriteria();
        if (!headerIds.isEmpty()) {
            existingHeaderCriteria.andIn("applyHeaderId", headerIds);
        } else {
            existingHeaderCriteria.andEqualTo("applyHeaderId", -1);
        }
        List<InvoiceApplyHeader> existingHeaders = invoiceApplyHeaderRepository.selectByCondition(existingHeadersCondition);

        Map<Long, InvoiceApplyHeader> headerMap = existingHeaders.stream()
                .collect(Collectors.toMap(InvoiceApplyHeader::getApplyHeaderId, Function.identity()));

        List<InvoiceApplyHeader> headersToUpdate = new ArrayList<>();

        for (Long headerId : headerIds) {
            List<InvoiceApplyLine> headerLines = linesGroupedByHeaderId.get(headerId);

            if (headerLines != null) {
                BigDecimal totalAmountSum = headerLines.stream()
                        .map(InvoiceApplyLine::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal taxAmountSum = headerLines.stream()
                        .map(InvoiceApplyLine::getTaxAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal excludeTaxAmountSum = headerLines.stream()
                        .map(InvoiceApplyLine::getExcludeTaxAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                InvoiceApplyHeader header = headerMap.get(headerId);
                if (header != null) {
                    header.setTotalAmount(totalAmountSum);
                    header.setTaxAmount(taxAmountSum);
                    header.setExcludeTaxAmount(excludeTaxAmountSum);

                    headersToUpdate.add(header);
                }
            }
        }

        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(headersToUpdate);

        return Boolean.TRUE;
    }
}
