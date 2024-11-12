package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.InvoiceApplyLineDto;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.InvoiceConstants;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyLine)应用服务
 *
 * @author muhammad.azzam@hand-global.com
 * @since 2024-11-04 11:37:36
 */
@Service
public class InvoiceApplyLineServiceImpl implements InvoiceApplyLineService {
    private final InvoiceApplyLineRepository invoiceApplyLineRepository;
    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    public InvoiceApplyLineServiceImpl(InvoiceApplyLineRepository invoiceApplyLineRepository, InvoiceApplyHeaderRepository invoiceApplyHeaderRepository) {
        this.invoiceApplyLineRepository = invoiceApplyLineRepository;
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
    }

    @Override
    public Page<InvoiceApplyLine> selectList(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {
        return PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyLineRepository.selectList(invoiceApplyLine));
    }

    @Override
    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {

        // Make update/insert lists
        List<InvoiceApplyLine> insertList = invoiceApplyLines.stream()
                .filter(line -> line.getApplyLineId() == null)
                .collect(Collectors.toList());

        List<InvoiceApplyLine> updateList = invoiceApplyLines.stream()
                .filter(line -> line.getApplyLineId() != null)
                .collect(Collectors.toList());

        // Process insert list
        for (InvoiceApplyLine line : insertList) {
            line.setTenantId(0L); // Set tenantId (consider passing tenantId from controller if needed)
            line.setTotalAmount(line.getUnitPrice().multiply(line.getQuantity()));
            line.setTaxAmount(line.getTotalAmount().multiply(line.getTaxRate()));
            line.setExcludeTaxAmount(line.getTotalAmount().subtract(line.getTaxAmount()));
        }

        if (!insertList.isEmpty()) {
            invoiceApplyLineRepository.batchInsertSelective(insertList);
        }

        // Process update list
        for (InvoiceApplyLine line : updateList) {
            InvoiceApplyLine dataLine = invoiceApplyLineRepository.selectByPrimary(line.getApplyLineId());
            if (dataLine == null) {
                throw new CommonException(InvoiceConstants.Exception.INVALID_LINE_HEADER, line.getApplyLineId());
            }

            // Re-Calc
            line.setTotalAmount(line.getUnitPrice().multiply(line.getQuantity()));
            line.setTaxAmount(line.getTotalAmount().multiply(line.getTaxRate()));
            line.setExcludeTaxAmount(line.getTotalAmount().subtract(line.getTaxAmount()));
        }

        if (!updateList.isEmpty()) {
            invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);
        }

        // Recalculate header totals
        for (InvoiceApplyLine line : invoiceApplyLines) {
            recalculateHeaderTotals(line.getApplyHeaderId());
        }
    }

    // method to recalculate header totals
    private void recalculateHeaderTotals(Long applyHeaderId) {
        InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectByPrimaryKey(applyHeaderId);
        if (header == null || header.getDelFlag().equals(1)) {
            throw new CommonException(InvoiceConstants.Exception.INVALID_APPLY_HEADER, applyHeaderId);
        }

        // Fetch lines per header and calculate totals
        List<InvoiceApplyLine> allLinesForHeader = invoiceApplyLineRepository.select("applyHeaderId", applyHeaderId);

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

    public void remove(List<InvoiceApplyLine> invoiceApplyLines) {
        if (invoiceApplyLines.isEmpty()) {
            throw new CommonException(InvoiceConstants.Exception.NO_DATA_PROVIDED);
        }

        // Group lines by applyHeaderId
        Map<Long, List<InvoiceApplyLine>> linesByHeaderId = invoiceApplyLines.stream()
                .collect(Collectors.groupingBy(InvoiceApplyLine::getApplyHeaderId));

        for (Map.Entry<Long, List<InvoiceApplyLine>> entry : linesByHeaderId.entrySet()) {
            Long applyHeaderId = entry.getKey();
            List<InvoiceApplyLine> linesToDelete = entry.getValue();

            // Delete lines for the current header
            invoiceApplyLineRepository.batchDeleteByPrimaryKey(linesToDelete);

            // Fetch remaining lines
            List<InvoiceApplyLine> remainingLines = invoiceApplyLineRepository.select("applyHeaderId", applyHeaderId);

            // Recalculate totals
            BigDecimal totalAmount = remainingLines.stream()
                    .map(InvoiceApplyLine::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal taxAmount = remainingLines.stream()
                    .map(InvoiceApplyLine::getTaxAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal excludeTaxAmount = remainingLines.stream()
                    .map(InvoiceApplyLine::getExcludeTaxAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Fetch the header and update totals
            InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectByPrimaryKey(applyHeaderId);
            if (header == null || header.getDelFlag().equals(1)) {
                throw new CommonException(InvoiceConstants.Exception.INVALID_APPLY_HEADER, applyHeaderId);
            }
            header.setTotalAmount(totalAmount);
            header.setTaxAmount(taxAmount);
            header.setExcludeTaxAmount(excludeTaxAmount);

            // Save the updated header
            invoiceApplyHeaderRepository.updateByPrimaryKeySelective(header);
        }
    }

    @Override
    public Page<InvoiceApplyLineDto> exportList(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {

        // Fetch paginated InvoiceApplyLine data
        Page<InvoiceApplyLine> pageLines = PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyLineRepository.selectList(invoiceApplyLine));

        List<InvoiceApplyLineDto> lineDTOS = pageLines.getContent().stream()
                .map(line -> {
                    InvoiceApplyLineDto lineDTO = new InvoiceApplyLineDto();

                    // Copy properties from entity to DTO
                    BeanUtils.copyProperties(line, lineDTO);

                    // fetch ApplyHeaderNumber
                    InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectByPrimaryKey(lineDTO.getApplyHeaderId());
                    if (header != null) {
                        lineDTO.setApplyHeaderNumber(header.getApplyHeaderNumber());
                    }

                    return lineDTO;
                })
                .collect(Collectors.toList());

        // Create a Page for InvoiceApplyLineDTO
        Page<InvoiceApplyLineDto> lineDTOsPage = new Page<>();
        lineDTOsPage.setContent(lineDTOS);
        lineDTOsPage.setTotalPages(pageLines.getTotalPages());
        lineDTOsPage.setTotalElements(pageLines.getTotalElements());
        lineDTOsPage.setNumber(pageLines.getNumber());
        lineDTOsPage.setSize(pageLines.getSize());

        return lineDTOsPage;
    }


}
