package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.dto.InvoiceApplyLineDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.InvoiceApplyConstants;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.ext.IllegalArgumentException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import lombok.AllArgsConstructor;
import org.hzero.core.message.MessageAccessor;
import org.hzero.mybatis.domian.Condition;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.beans.BeanUtils;
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
 * @author
 * @since 2024-11-04 11:44:51
 */
@Service
@AllArgsConstructor
public class InvoiceApplyLineServiceImpl implements InvoiceApplyLineService {
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Override
    public Page<InvoiceApplyLine> selectList(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {
        return PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyLineRepository.selectList(invoiceApplyLine));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {
        Set<Long> currHeaderIds = getCurrentHeaderIds(invoiceApplyLines);

        validateHeaderIds(currHeaderIds);

        List<InvoiceApplyLine> saveApplyLines = calculateAmounts(invoiceApplyLines);

        List<InvoiceApplyLine> insertList = saveApplyLines.stream()
                .filter(line -> line.getApplyLineId() == null)
                .collect(Collectors.toList());

        List<InvoiceApplyLine> updateList = saveApplyLines.stream()
                .filter(line -> line.getApplyLineId() != null)
                .collect(Collectors.toList());

        performInsert(insertList);
        performUpdate(updateList);

        Set<Long> headerIds = collectHeaderIds(insertList, updateList);
        Map<Long, List<InvoiceApplyLine>> linesGroupedByHeaderId = getLinesGroupedByHeaderId(headerIds);
        Map<Long, InvoiceApplyHeader> headerMap = getHeaderMap(headerIds);

        List<InvoiceApplyHeader> headersToUpdate = calculateHeaderAmounts(headerIds, linesGroupedByHeaderId, headerMap);

        updateHeaders(headersToUpdate);
    }

    private Set<Long> getCurrentHeaderIds(List<InvoiceApplyLine> invoiceApplyLines) {
        return invoiceApplyLines.stream()
                .map(InvoiceApplyLine::getApplyHeaderId)
                .collect(Collectors.toSet());
    }

    private void validateHeaderIds(Set<Long> currHeaderIds) {
        Condition condition = new Condition(InvoiceApplyHeader.class);
        condition.createCriteria().andEqualTo("delFlag", 0).andIn("applyHeaderId", currHeaderIds);

        List<Long> existingHeaderIds = invoiceApplyHeaderRepository.selectByCondition(condition)
                .stream()
                .map(InvoiceApplyHeader::getApplyHeaderId)
                .collect(Collectors.toList());

        currHeaderIds.removeAll(existingHeaderIds);

        if (!currHeaderIds.isEmpty()) {
            String error = "The following header IDs are missing in the InvoiceApplyHeader table or have been deleted: " + currHeaderIds;
            throw new CommonException(InvoiceApplyConstants.INV_APPLY_LINE_ERROR, error);
        }
    }

    private List<InvoiceApplyLine> calculateAmounts(List<InvoiceApplyLine> invoiceApplyLines) {
        return invoiceApplyLines.stream()
                .peek(line -> {
                    line.setTotalAmount(line.getUnitPrice().multiply(line.getQuantity()));
                    line.setTaxAmount(line.getTotalAmount().multiply(line.getTaxRate()));
                    line.setExcludeTaxAmount(line.getTotalAmount().subtract(line.getTaxAmount()));
                })
                .collect(Collectors.toList());
    }

    private void performInsert(List<InvoiceApplyLine> insertList) {
        if (!insertList.isEmpty()) {
            invoiceApplyLineRepository.batchInsertSelective(insertList);
        }
    }

    private void performUpdate(List<InvoiceApplyLine> updateList) {
        if (!updateList.isEmpty()) {
            invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);
        }
    }

    private Set<Long> collectHeaderIds(List<InvoiceApplyLine> insertList, List<InvoiceApplyLine> updateList) {
        return Stream.concat(insertList.stream(), updateList.stream())
                .map(InvoiceApplyLine::getApplyHeaderId)
                .collect(Collectors.toSet());
    }

    private Map<Long, List<InvoiceApplyLine>> getLinesGroupedByHeaderId(Set<Long> headerIds) {
        Condition condition = new Condition(InvoiceApplyLine.class);
        Condition.Criteria criteria = condition.createCriteria();
        if (!headerIds.isEmpty()) {
            criteria.andIn("applyHeaderId", headerIds);
        } else {
            criteria.andEqualTo("applyHeaderId", -1);
        }
        List<InvoiceApplyLine> allLinesForHeaders = invoiceApplyLineRepository.selectByCondition(condition);

        return allLinesForHeaders.stream()
                .collect(Collectors.groupingBy(InvoiceApplyLine::getApplyHeaderId));
    }

    private Map<Long, InvoiceApplyHeader> getHeaderMap(Set<Long> headerIds) {
        Condition condition = new Condition(InvoiceApplyHeader.class);
        condition.createCriteria().andIn("applyHeaderId", headerIds);

        List<InvoiceApplyHeader> existingHeaders = invoiceApplyHeaderRepository.selectByCondition(condition);
        return existingHeaders.stream()
                .collect(Collectors.toMap(InvoiceApplyHeader::getApplyHeaderId, Function.identity()));
    }

    private List<InvoiceApplyHeader> calculateHeaderAmounts(Set<Long> headerIds, Map<Long, List<InvoiceApplyLine>> linesGroupedByHeaderId, Map<Long, InvoiceApplyHeader> headerMap) {
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
        return headersToUpdate;
    }

    private void updateHeaders(List<InvoiceApplyHeader> headersToUpdate) {
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(headersToUpdate);
    }


    @Override
    public void remove(List<InvoiceApplyLine> invoiceApplyLines) {
        invoiceApplyLineRepository.batchDeleteByPrimaryKey(invoiceApplyLines);

        Set<Long> headersIDsInApplyLines = invoiceApplyLines.stream().map(InvoiceApplyLine::getApplyHeaderId).collect(Collectors.toSet());

        Map<Long, List<InvoiceApplyLine>> linesByHeaderId = getLinesGroupedByHeaderId(headersIDsInApplyLines);

        Map<Long, InvoiceApplyHeader> headerMap = getHeaderMap(headersIDsInApplyLines);
        List<InvoiceApplyHeader> headersToUpdate = calculateHeaderAmounts(headersIDsInApplyLines, linesByHeaderId, headerMap);
        updateHeaders(headersToUpdate);
    }

    @Override
    public Page<InvoiceApplyLineDTO> exportLine(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {
        String applyHeaderNumber = invoiceApplyHeaderRepository
                .selectByPrimary( invoiceApplyLine.getApplyHeaderId())
                .getApplyHeaderNumber();

        Page<InvoiceApplyLine> applyLines = PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyLineRepository.selectList(invoiceApplyLine));

        List<InvoiceApplyLineDTO> applyLineDTOS = applyLines.stream()
                .map(line -> {
                    InvoiceApplyLineDTO lineDto = new InvoiceApplyLineDTO();
                    BeanUtils.copyProperties(line, lineDto);
                    lineDto.setApplyHeaderNumber(applyHeaderNumber);
                    return lineDto;
                })
                .collect(Collectors.toList());

        Page<InvoiceApplyLineDTO> lineDTOsPage = new Page<>();
        lineDTOsPage.setContent(applyLineDTOS);
        lineDTOsPage.setTotalPages(applyLines.getTotalPages());
        lineDTOsPage.setTotalElements(applyLines.getTotalElements());
        lineDTOsPage.setNumber(applyLines.getNumber());
        lineDTOsPage.setSize(applyLines.getSize());

        return lineDTOsPage;
    }
}

