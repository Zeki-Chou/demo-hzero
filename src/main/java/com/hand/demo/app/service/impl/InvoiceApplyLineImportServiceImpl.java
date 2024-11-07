package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import io.choerodon.core.exception.CommonException;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.hzero.core.redis.RedisHelper;
import org.hzero.mybatis.domian.Condition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ImportService(templateCode = "EXAM-47358-HEADER", sheetName = "INVOICE_APPLY_LINE")
public class InvoiceApplyLineImportServiceImpl extends BatchImportHandler {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    RedisHelper redisHelper;

    @Override
    public Boolean doImport(List<String> data) {
        List<InvoiceApplyLine> invoiceApplyLines = new ArrayList<>();

        for (String line : data) {
            if (!line.isEmpty() || line.length() != 0) {
                try {
                    InvoiceApplyLine invoiceApplyLine = objectMapper.readValue(line, InvoiceApplyLine.class);
                    invoiceApplyLines.add(invoiceApplyLine);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        try {
            List<InvoiceApplyLine> insertList = invoiceApplyLines.stream()
                    .filter(line -> line.getApplyHeaderId() == null)
                    .collect(Collectors.toList());

            List<InvoiceApplyLine> updateList = invoiceApplyLines.stream()
                    .filter(line -> line.getApplyHeaderId() != null)
                    .collect(Collectors.toList());

            for (InvoiceApplyLine line : insertList) {
                line.setTenantId(0L);
                line.setTotalAmount(line.getUnitPrice().multiply(line.getQuantity()));
                line.setTaxAmount(line.getTotalAmount().multiply(line.getTaxRate()));
                line.setExcludeTaxAmount(line.getTotalAmount().subtract(line.getTaxAmount()));
            }

            if (!insertList.isEmpty()) {
                invoiceApplyLineRepository.batchInsertSelective(insertList);
            }

            for (InvoiceApplyLine line : updateList) {
                InvoiceApplyLine dataLine = invoiceApplyLineRepository.selectByPrimary(line.getApplyLineId());
                line.setObjectVersionNumber(dataLine.getObjectVersionNumber());
                line.setTotalAmount(line.getUnitPrice().multiply(line.getQuantity()));
                line.setTaxAmount(line.getTotalAmount().multiply(line.getTaxRate()));
                line.setExcludeTaxAmount(line.getTotalAmount().subtract(line.getTaxAmount()));
            }

            if (!updateList.isEmpty()) {
                invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);
            }

            Set<Long> affectedHeaderIds = invoiceApplyLines.stream()
                    .map(InvoiceApplyLine::getApplyHeaderId)
                    .collect(Collectors.toSet());

            for (Long headerId : affectedHeaderIds) {
                InvoiceApplyHeader headerData = invoiceApplyHeaderRepository.selectByPrimaryKey(headerId);
                InvoiceApplyHeaderDTO headerDTO = changeToDTO(headerData);

                Condition condition = new Condition(InvoiceApplyHeader.class);
                Condition.Criteria criteria = condition.createCriteria();
                criteria.andEqualTo("applyHeaderId",headerDTO.getApplyHeaderId());
                List<InvoiceApplyLine> associatedLines = invoiceApplyLineRepository.selectByCondition(condition);
                headerDTO.setInvoiceApplyLines(associatedLines);

                BigDecimal totalAmount = associatedLines.stream()
                        .map(InvoiceApplyLine::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal excludeTaxAmount = associatedLines.stream()
                        .map(InvoiceApplyLine::getExcludeTaxAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal taxAmount = associatedLines.stream()
                        .map(InvoiceApplyLine::getTaxAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (headerData != null) {
                    headerData.setTotalAmount(totalAmount);
                    headerData.setExcludeTaxAmount(excludeTaxAmount);
                    headerData.setTaxAmount(taxAmount);
                    redisHelper.delKey(headerData.getApplyHeaderNumber());
                    invoiceApplyHeaderRepository.updateByPrimaryKey(headerData);
                }
            }

            return true;
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    private InvoiceApplyHeaderDTO changeToDTO(InvoiceApplyHeader invoiceApplyHeaderDTO) {
        InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();
        BeanUtils.copyProperties(invoiceApplyHeaderDTO, dto);
        return dto;
    }
}
