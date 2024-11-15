package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.InvHeaderConstant;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
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
import java.util.function.Function;
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
        List<InvoiceApplyLine> invoiceApplyLines = parseDataToInvoiceApplyLines(data);
        DetailsHelper.getUserDetails().getAdmin();

        try {
            List<InvoiceApplyLine> insertList = filterInsertList(invoiceApplyLines);
            List<InvoiceApplyLine> updateList = filterUpdateList(invoiceApplyLines);

            prepareInsertList(insertList);
            if (!insertList.isEmpty()) {
                invoiceApplyLineRepository.batchInsertSelective(insertList);
            }

            prepareUpdateList(updateList);
            if (!updateList.isEmpty()) {
                invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);
            }

            updateAffectedHeaders(invoiceApplyLines);
            return true;

        } catch (Exception e) {
            throw new CommonException(e);
        }
    }

    private List<InvoiceApplyLine> parseDataToInvoiceApplyLines(List<String> data) {
        List<InvoiceApplyLine> invoiceApplyLines = new ArrayList<>();
        for (String line : data) {
            if (!line.isEmpty()) {
                try {
                    InvoiceApplyLine invoiceApplyLine = objectMapper.readValue(line, InvoiceApplyLine.class);
                    invoiceApplyLines.add(invoiceApplyLine);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return invoiceApplyLines;
    }

    private List<InvoiceApplyLine> filterInsertList(List<InvoiceApplyLine> lines) {
        return lines.stream()
                .filter(line -> line.getApplyHeaderId() == null)
                .collect(Collectors.toList());
    }

    private List<InvoiceApplyLine> filterUpdateList(List<InvoiceApplyLine> lines) {
        return lines.stream()
                .filter(line -> line.getApplyHeaderId() != null)
                .collect(Collectors.toList());
    }

    private void prepareInsertList(List<InvoiceApplyLine> insertList) {
        for (InvoiceApplyLine line : insertList) {
            line.setTenantId(0L);
            calculateLineAmounts(line);
        }
    }

    private void prepareUpdateList(List<InvoiceApplyLine> updateList) {
        for (InvoiceApplyLine line : updateList) {
            InvoiceApplyLine dataLine = invoiceApplyLineRepository.selectByPrimary(line.getApplyLineId());
            line.setObjectVersionNumber(dataLine.getObjectVersionNumber());
            calculateLineAmounts(line);
        }
    }

    private void calculateLineAmounts(InvoiceApplyLine line) {
        line.setTotalAmount(line.getUnitPrice().multiply(line.getQuantity()));
        line.setTaxAmount(line.getTotalAmount().multiply(line.getTaxRate()));
        line.setExcludeTaxAmount(line.getTotalAmount().subtract(line.getTaxAmount()));
    }

    private void updateAffectedHeaders(List<InvoiceApplyLine> invoiceApplyLines) {
        Set<Long> affectedHeaderIds = invoiceApplyLines.stream()
                .map(InvoiceApplyLine::getApplyHeaderId)
                .collect(Collectors.toSet());

        for (Long headerId : affectedHeaderIds) {
            if (headerId != null) {
                updateHeaderAmounts(headerId);
            }
        }
    }

    private void updateHeaderAmounts(Long headerId) {
        InvoiceApplyHeader headerData = invoiceApplyHeaderRepository.selectByPrimaryKey(headerId);
        if (headerData != null) {
            InvoiceApplyHeaderDTO headerDTO = changeToDTO(headerData);

            List<InvoiceApplyLine> associatedLines = getAssociatedLines(headerDTO.getApplyHeaderId());
            headerDTO.setInvoiceApplyLines(associatedLines);

            BigDecimal totalAmount = calculateTotalAmount(associatedLines, InvoiceApplyLine::getTotalAmount);
            BigDecimal excludeTaxAmount = calculateTotalAmount(associatedLines, InvoiceApplyLine::getExcludeTaxAmount);
            BigDecimal taxAmount = calculateTotalAmount(associatedLines, InvoiceApplyLine::getTaxAmount);

            headerData.setTotalAmount(totalAmount);
            headerData.setExcludeTaxAmount(excludeTaxAmount);
            headerData.setTaxAmount(taxAmount);
            redisHelper.delKey(headerData.getApplyHeaderId() + InvHeaderConstant.PREFIX);
            invoiceApplyHeaderRepository.updateByPrimaryKey(headerData);
        }
    }

    private List<InvoiceApplyLine> getAssociatedLines(Long applyHeaderId) {
        Condition condition = new Condition(InvoiceApplyHeader.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo("applyHeaderId", applyHeaderId);
        return invoiceApplyLineRepository.selectByCondition(condition);
    }

    private BigDecimal calculateTotalAmount(List<InvoiceApplyLine> lines, Function<InvoiceApplyLine, BigDecimal> mapper) {
        return lines.stream()
                .map(mapper)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    private InvoiceApplyHeaderDTO changeToDTO(InvoiceApplyHeader invoiceApplyHeaderDTO) {
        InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();
        BeanUtils.copyProperties(invoiceApplyHeaderDTO, dto);
        return dto;
    }
}
