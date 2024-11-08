package com.hand.demo.app.service.impl;

import com.hand.demo.app.service.InvoiceApplyHeaderService;
import com.hand.demo.domain.dto.InvoiceApplyLineDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import lombok.AllArgsConstructor;
import org.hzero.mybatis.domian.Condition;
import org.springframework.beans.BeanUtils;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * (InvoiceApplyLine)应用服务
 *
 * @author azhar.naufal@hand-global.com
 * @since 2024-11-04 11:16:15
 */
@Service
@AllArgsConstructor
public class InvoiceApplyLineServiceImpl implements InvoiceApplyLineService {
    private final InvoiceApplyLineRepository invoiceApplyLineRepository;
    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    private final InvoiceApplyHeaderService headerService;

    @Override
    public Page<InvoiceApplyLine> selectList(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {
        return PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyLineRepository.selectList(invoiceApplyLine));
    }

    @Override
    public Page<InvoiceApplyLineDTO> selectListExport(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {
        Page<InvoiceApplyLine> pageLines = PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyLineRepository.selectList(invoiceApplyLine));

        List<InvoiceApplyLineDTO> lineDTOS = pageLines.getContent().stream()
                .map(line -> {
                    InvoiceApplyLineDTO lineDTO = new InvoiceApplyLineDTO();
                    BeanUtils.copyProperties(line, lineDTO);
                    lineDTO.setApplyHeaderNumber(getHeaderNumber(lineDTO.getApplyHeaderId()));
                    return lineDTO;
                })
                .collect(Collectors.toList());

        Page<InvoiceApplyLineDTO> lineDTOsPage = new Page<>();
        lineDTOsPage.setContent(lineDTOS);
        lineDTOsPage.setTotalPages(pageLines.getTotalPages());
        lineDTOsPage.setTotalElements(pageLines.getTotalElements());
        lineDTOsPage.setNumber(pageLines.getNumber());
        lineDTOsPage.setSize(pageLines.getSize());

        return lineDTOsPage;
    }

    @Override
    public List<InvoiceApplyLine> linesByHeaderId(Long headerId){
        return invoiceApplyLineRepository.select("applyHeaderId", headerId);
    }

//    @Override
//    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {
//        for(InvoiceApplyLine invoiceApplyLine : invoiceApplyLines){
//            InvoiceApplyHeader header = headerService.selectById(invoiceApplyLine.getApplyHeaderId());
//            if(header != null){
//                if(header.getDelFlag() == 0){
//                    invoiceApplyLine.setTotalAmount(invoiceApplyLine.getQuantity().multiply(invoiceApplyLine.getUnitPrice()));
//                    invoiceApplyLine.setTaxAmount(invoiceApplyLine.getTotalAmount().multiply(invoiceApplyLine.getTaxRate()));
//                    invoiceApplyLine.setExcludeTaxAmount(invoiceApplyLine.getTotalAmount().subtract(invoiceApplyLine.getTaxAmount()));
//
//                    if(invoiceApplyLine.getApplyLineId() == null){
//                        invoiceApplyLineRepository.insert(invoiceApplyLine);
//                        recalculateAndUpdateHeaderTotals(header);
//                    }else {
//                        invoiceApplyLineRepository.updateByPrimaryKeySelective(invoiceApplyLine);
//                        recalculateAndUpdateHeaderTotals(header);
//                    }
//                }
//            }
//        }
//    }

    @Override
    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {
        // 1. Validate headers that exist and are not deleted
        Set<Long> headerIds = getHeaderIds(invoiceApplyLines);
        validateHeaders(headerIds);

        // 2. Prepare invoice lines and calculate totals (totalAmount, taxAmount, excludeTaxAmount)
        List<InvoiceApplyLine> saveApplyLines = prepareInvoiceApplyLines(invoiceApplyLines);

        // 3. Separate lines into insert and update categories
        List<InvoiceApplyLine> insertList = getInsertLines(saveApplyLines);
        List<InvoiceApplyLine> updateList = getUpdateLines(saveApplyLines);

        // 4. Perform batch insert and update operations
        batchInsert(insertList);
        batchUpdate(updateList);

        // 5. Recalculate totals for each header after insert and update operations
        updateHeaderTotals(insertList, updateList);
    }

    // Extract the set of header IDs from the list of invoice lines
    private Set<Long> getHeaderIds(List<InvoiceApplyLine> invoiceApplyLines) {
        return invoiceApplyLines.stream()
                .map(InvoiceApplyLine::getApplyHeaderId)
                .collect(Collectors.toSet());
    }

    // Validate if all headers exist in the database and are not marked as deleted (delFlag = 0)
    private void validateHeaders(Set<Long> headerIds) {
        Condition condition = new Condition(InvoiceApplyHeader.class);
        condition.createCriteria().andEqualTo("delFlag", 0).andIn("applyHeaderId", headerIds);
        List<Long> existingHeaderIds = invoiceApplyHeaderRepository.selectByCondition(condition)
                .stream()
                .map(InvoiceApplyHeader::getApplyHeaderId)
                .collect(Collectors.toList());

        headerIds.removeAll(existingHeaderIds);
        if (!headerIds.isEmpty()) {
            throw new IllegalArgumentException("The following header IDs are either missing or deleted: " + headerIds);
        }
    }

    // Calculate the total amounts, tax, and exclude tax for each invoice line
    private List<InvoiceApplyLine> prepareInvoiceApplyLines(List<InvoiceApplyLine> invoiceApplyLines) {
        for (InvoiceApplyLine line : invoiceApplyLines) {
            // Calculate total amount, tax, and exclude tax for each line
            line.setTotalAmount(line.getUnitPrice().multiply(line.getQuantity()));
            line.setTaxAmount(line.getTotalAmount().multiply(line.getTaxRate()));
            line.setExcludeTaxAmount(line.getTotalAmount().subtract(line.getTaxAmount()));
        }
        return invoiceApplyLines;
    }

    // Filter the lines that need to be inserted (those with no applyLineId)
    private List<InvoiceApplyLine> getInsertLines(List<InvoiceApplyLine> saveApplyLines) {
        return saveApplyLines.stream()
                .filter(line -> line.getApplyLineId() == null)
                .collect(Collectors.toList());
    }

    // Filter the lines that need to be updated (those with an existing applyLineId)
    private List<InvoiceApplyLine> getUpdateLines(List<InvoiceApplyLine> saveApplyLines) {
        return saveApplyLines.stream()
                .filter(line -> line.getApplyLineId() != null)
                .collect(Collectors.toList());
    }

    // Perform batch insert operation for new invoice lines
    private void batchInsert(List<InvoiceApplyLine> insertList) {
        if (!insertList.isEmpty()) {
            invoiceApplyLineRepository.batchInsertSelective(insertList);
        }
    }

    // Perform batch update operation for existing invoice lines
    private void batchUpdate(List<InvoiceApplyLine> updateList) {
        if (!updateList.isEmpty()) {
            invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);
        }
    }

    // Update header totals based on all affected lines (both new and updated)
    private void updateHeaderTotals(List<InvoiceApplyLine> insertList, List<InvoiceApplyLine> updateList) {
        // Get all the affected header IDs from both insert and update lists
        Set<Long> headerIds = Stream.concat(insertList.stream(), updateList.stream())
                .map(InvoiceApplyLine::getApplyHeaderId)
                .collect(Collectors.toSet());

        // Retrieve all lines related to these headers
        List<InvoiceApplyLine> allLinesForHeaders = getAllLinesForHeaders(headerIds);

        // Group the lines by their respective header IDs
        Map<Long, List<InvoiceApplyLine>> linesGroupedByHeaderId = groupLinesByHeaderId(allLinesForHeaders);

        // Calculate the new totals for each header
        List<InvoiceApplyHeader> headersToUpdate = calculateHeaderTotals(headerIds, linesGroupedByHeaderId);

        // Update the headers with the new totals
        if (!headersToUpdate.isEmpty()) {
            invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(headersToUpdate);
        }
    }

    // Retrieve all invoice lines associated with the given header IDs
    private List<InvoiceApplyLine> getAllLinesForHeaders(Set<Long> headerIds) {
        Condition condition = new Condition(InvoiceApplyLine.class);
        condition.createCriteria().andIn("applyHeaderId", headerIds);
        return invoiceApplyLineRepository.selectByCondition(condition);
    }

    // Group invoice lines by their respective applyHeaderId
    private Map<Long, List<InvoiceApplyLine>> groupLinesByHeaderId(List<InvoiceApplyLine> allLinesForHeaders) {
        return allLinesForHeaders.stream()
                .collect(Collectors.groupingBy(InvoiceApplyLine::getApplyHeaderId));
    }

    // Calculate the new total, tax, and excludeTaxAmount for each header based on its lines
    private List<InvoiceApplyHeader> calculateHeaderTotals(Set<Long> headerIds, Map<Long, List<InvoiceApplyLine>> linesGroupedByHeaderId) {
        List<InvoiceApplyHeader> headersToUpdate = new ArrayList<>();

        for (Long headerId : headerIds) {
            List<InvoiceApplyLine> headerLines = linesGroupedByHeaderId.get(headerId);
            if (headerLines != null) {
                BigDecimal totalAmountSum = calculateSum(headerLines, InvoiceApplyLine::getTotalAmount);
                BigDecimal taxAmountSum = calculateSum(headerLines, InvoiceApplyLine::getTaxAmount);
                BigDecimal excludeTaxAmountSum = calculateSum(headerLines, InvoiceApplyLine::getExcludeTaxAmount);

                InvoiceApplyHeader header = new InvoiceApplyHeader();
                header.setApplyHeaderId(headerId);
                header.setTotalAmount(totalAmountSum);
                header.setTaxAmount(taxAmountSum);
                header.setExcludeTaxAmount(excludeTaxAmountSum);

                headersToUpdate.add(header);
            }
        }

        return headersToUpdate;
    }

    // Calculate the sum of a specific field (totalAmount, taxAmount, or excludeTaxAmount) for all lines
    private BigDecimal calculateSum(List<InvoiceApplyLine> lines, Function<InvoiceApplyLine, BigDecimal> mapper) {
        return lines.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteData(List<InvoiceApplyLine> invoiceApplyLines){
        invoiceApplyLineRepository.batchDeleteByPrimaryKey(invoiceApplyLines);
        List<Long> headerIdList = new LinkedList<>();
        for(InvoiceApplyLine line : invoiceApplyLines){
            Long headerId = line.getApplyHeaderId();
            headerIdList.add(headerId);
        }
        for(Long id : headerIdList){
            InvoiceApplyHeader invoiceApplyHeader = headerService.selectById(id);
            recalculateAndUpdateHeaderTotals(invoiceApplyHeader);
        }
    }

    private void recalculateAndUpdateHeaderTotals(InvoiceApplyHeader savedHeader) {
        BigDecimal newTotalAmount = BigDecimal.ZERO;
        BigDecimal newExcludeTaxAmount = BigDecimal.ZERO;
        BigDecimal newTaxAmount = BigDecimal.ZERO;

        List<InvoiceApplyLine> updatedLines = invoiceApplyLineRepository.selectByHeaderId(savedHeader.getApplyHeaderId());
        for (InvoiceApplyLine line : updatedLines) {
            newTotalAmount = newTotalAmount.add(line.getTotalAmount() != null ? line.getTotalAmount() : BigDecimal.ZERO);
            newExcludeTaxAmount = newExcludeTaxAmount.add(line.getExcludeTaxAmount() != null ? line.getExcludeTaxAmount() : BigDecimal.ZERO);
            newTaxAmount = newTaxAmount.add(line.getTaxAmount() != null ? line.getTaxAmount() : BigDecimal.ZERO);
        }

        savedHeader.setTotalAmount(newTotalAmount);
        savedHeader.setExcludeTaxAmount(newExcludeTaxAmount);
        savedHeader.setTaxAmount(newTaxAmount);

        headerService.updateByPrimaryKeySelective(savedHeader);
    }

    private String getHeaderNumber (Long headerId){
        InvoiceApplyHeader header = headerService.getHeaderById(headerId);
        return header.getApplyHeaderNumber();
    }
}

