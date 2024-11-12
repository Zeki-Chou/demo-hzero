package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import io.choerodon.core.exception.CommonException;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ImportService(templateCode = "EXAM-47357-HEADER", sheetName = "INV_APPLY_LINES")
public class InvoiceApplyLineImportServiceImpl extends BatchImportHandler {

    private final ObjectMapper objectMapper;
    private final InvoiceApplyLineRepository invoiceApplyLineRepository;
    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    public InvoiceApplyLineImportServiceImpl(ObjectMapper objectMapper, InvoiceApplyHeaderRepository invoiceApplyHeaderRepository, InvoiceApplyLineRepository invoiceApplyLineRepository) {
        this.objectMapper = objectMapper;
        this.invoiceApplyLineRepository = invoiceApplyLineRepository;
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
    }

    @Override
    public Boolean doImport(List<String> data) {
        try {
            List<InvoiceApplyLine> invoiceApplyLines = new ArrayList<>();

            // Parse data and convert each JSON string to an InvoiceApplyLine object
            for (String jsonData : data) {
                InvoiceApplyLine invoiceApplyLine = objectMapper.readValue(jsonData, InvoiceApplyLine.class);
                invoiceApplyLines.add(invoiceApplyLine);
            }

            // Separate lists for insert and update operations based on applyLineId
            List<InvoiceApplyLine> insertList = invoiceApplyLines.stream()
                    .filter(line -> line.getApplyLineId() == null)
                    .collect(Collectors.toList());

            List<InvoiceApplyLine> updateList = invoiceApplyLines.stream()
                    .filter(line -> line.getApplyLineId() != null)
                    .collect(Collectors.toList());

            // Process insert list: calculate amounts and set tenantId
            for (InvoiceApplyLine line : insertList) {
                line.setTenantId(0L);
                line.setTotalAmount(line.getUnitPrice().multiply(line.getQuantity()));
                line.setTaxAmount(line.getTotalAmount().multiply(line.getTaxRate()));
                line.setExcludeTaxAmount(line.getTotalAmount().subtract(line.getTaxAmount()));
            }

            // Batch insert if the insert list is not empty
            if (!insertList.isEmpty()) {
                invoiceApplyLineRepository.batchInsertSelective(insertList);
            }

            // Process update list: fetch current version, calculate amounts, and set version number
            for (InvoiceApplyLine line : updateList) {
                InvoiceApplyLine dataLine = invoiceApplyLineRepository.selectByPrimary(line.getApplyLineId());
                if (dataLine == null) {
                    throw new CommonException("Invalid apply_line_id: " + line.getApplyLineId());
                }
                line.setTotalAmount(line.getUnitPrice().multiply(line.getQuantity()));
                line.setTaxAmount(line.getTotalAmount().multiply(line.getTaxRate()));
                line.setExcludeTaxAmount(line.getTotalAmount().subtract(line.getTaxAmount()));
            }

            // Batch update if the update list is not empty
            if (!updateList.isEmpty()) {
                invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);
            }

            // Group lines by applyHeaderId
            Map<Long, List<InvoiceApplyLine>> headerLineMap = invoiceApplyLines.stream()
                    .collect(Collectors.groupingBy(InvoiceApplyLine::getApplyHeaderId));

            for (Map.Entry<Long, List<InvoiceApplyLine>> entry : headerLineMap.entrySet()) {
                Long applyHeaderId = entry.getKey();

                InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectByPrimaryKey(applyHeaderId);
                if (header == null || header.getDelFlag().equals(1)) {
                    throw new CommonException("Invalid or deleted apply_header_id: " + applyHeaderId);
                }

                // Re-fetch all lines
                List<InvoiceApplyLine> allLinesForHeader = invoiceApplyLineRepository.select("applyHeaderId", applyHeaderId);

                // Recalculate totals using the re-fetched lines
                BigDecimal totalAmountSum = allLinesForHeader.stream()
                        .map(InvoiceApplyLine::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal taxAmountSum = allLinesForHeader.stream()
                        .map(InvoiceApplyLine::getTaxAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal excludeTaxAmountSum = allLinesForHeader.stream()
                        .map(InvoiceApplyLine::getExcludeTaxAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Update header with recalculated sums
                header.setTotalAmount(totalAmountSum);
                header.setTaxAmount(taxAmountSum);
                header.setExcludeTaxAmount(excludeTaxAmountSum);

                // Save updated header
                invoiceApplyHeaderRepository.updateByPrimaryKeySelective(header);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
