package com.hand.demo.app.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import com.hand.demo.domain.dto.InvoiceApplyLineDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.InvoiceApplyHeaderConstant;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.mybatis.domian.Condition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyLine)应用服务
 *
 * @author azhar.naufal@hand-global.com
 * @since 2024-11-04 11:16:15
 */
@Service
public class InvoiceApplyLineServiceImpl implements InvoiceApplyLineService {
    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;
    @Autowired
    private InvoiceApplyHeaderService headerService;
    @Autowired
    private InvoiceApplyHeaderRepository headerRepository;

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
    public List<InvoiceApplyLine> linesByHeaderId(Long headerId) {
        return invoiceApplyLineRepository.select("applyHeaderId", headerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {
        // Collect header IDs from lines
        Set<Long> requestHeaderIdsSet = invoiceApplyLines.stream()
                .map(InvoiceApplyLine::getApplyHeaderId)
                .collect(Collectors.toSet());

        //Query existing headers
        Condition condition = new Condition(InvoiceApplyHeader.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andIn(InvoiceApplyHeader.FIELD_APPLY_HEADER_ID, requestHeaderIdsSet)
                .andEqualTo(InvoiceApplyHeader.FIELD_DEL_FLAG, 0);

        List<InvoiceApplyHeader> existingHeaders = headerRepository.selectByCondition(condition);

        //Create Existing Map and Remove matching IDs from requestHeaderIdsSet
        Map<Long, InvoiceApplyHeader> existingHeaderMap = new HashMap<>();
        for (InvoiceApplyHeader header : existingHeaders) {
            existingHeaderMap.put(header.getApplyHeaderId(), header);
            requestHeaderIdsSet.remove(header.getApplyHeaderId());
        }

        //Check if there some notFound Header
        if (CollUtil.isNotEmpty(requestHeaderIdsSet)) {
            String notFoundIdsString = requestHeaderIdsSet.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            throw new CommonException(InvoiceApplyHeaderConstant.ERROR_NOT_FOUND, notFoundIdsString);
        }


        //Conditioning there request are insert or update
        List<InvoiceApplyLine> insertList = new LinkedList<>();
        List<InvoiceApplyLine> updateList = new LinkedList<>();

        for (InvoiceApplyLine invoiceApplyLine : invoiceApplyLines) {
            //Set Amount
            invoiceApplyLine.setTotalAmount(invoiceApplyLine.getQuantity().multiply(invoiceApplyLine.getUnitPrice()));
            invoiceApplyLine.setTaxAmount(invoiceApplyLine.getTotalAmount().multiply(invoiceApplyLine.getTaxRate()));
            invoiceApplyLine.setExcludeTaxAmount(invoiceApplyLine.getTotalAmount().subtract(invoiceApplyLine.getTaxAmount()));

            if (invoiceApplyLine.getApplyLineId() == null) {
                insertList.add(invoiceApplyLine);
            } else {
                updateList.add(invoiceApplyLine);
            }
        }

        //Insert or Update
        invoiceApplyLineRepository.batchInsert(insertList);
        invoiceApplyLineRepository.batchUpdateByPrimaryKey(updateList);

        //Recalculate for all header exists
        batchRecalculateAndUpdateHeaderTotals(existingHeaders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteData(List<InvoiceApplyLine> invoiceApplyLines) {
        invoiceApplyLineRepository.batchDeleteByPrimaryKey(invoiceApplyLines);
        //Collect ids for update for each header
        List<Long> headerIdList = new LinkedList<>();
        for (InvoiceApplyLine line : invoiceApplyLines) {
            Long headerId = line.getApplyHeaderId();
            headerIdList.add(headerId);
        }

        //Create String ids
        String headerIds = headerIdList.stream()
                .map(String::valueOf)  // Convert Long to String
                .collect(Collectors.joining(","));

        //Query listHeader by Ids
        List<InvoiceApplyHeader> headers = headerRepository.selectByIds(headerIds);

        //Batch Recalculate for All headers when each line deleted
        batchRecalculateAndUpdateHeaderTotals(headers);

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

        headerRepository.updateOptional(savedHeader, InvoiceApplyHeader.FIELD_TOTAL_AMOUNT,
                InvoiceApplyHeader.FIELD_EXCLUDE_TAX_AMOUNT, InvoiceApplyHeader.FIELD_TAX_AMOUNT);
    }

    private String getHeaderNumber(Long headerId) {
        InvoiceApplyHeader header = headerService.getHeaderById(headerId);
        return header.getApplyHeaderNumber();
    }

    private void batchRecalculateAndUpdateHeaderTotals(List<InvoiceApplyHeader> savedHeaders) {
        //Create headerIdsLong
        Set<Long> headerIdsLong = new HashSet<>();
        for (InvoiceApplyHeader header : savedHeaders) {
            headerIdsLong.add(header.getApplyHeaderId());
        }

        //Condition for query all lines for each saveHeader
        Condition conditionLineByHeader = new Condition(InvoiceApplyLine.class);
        Condition.Criteria criteriaLineByHeader = conditionLineByHeader.createCriteria();
        criteriaLineByHeader.andIn("applyHeaderId", headerIdsLong);

        List<InvoiceApplyLine> linesByHeader = invoiceApplyLineRepository.selectByCondition(conditionLineByHeader);

        //Mapping lines for each header
        Map<Long, List<InvoiceApplyLine>> linesGroupedByHeaderId = linesByHeader.stream()
                .collect(Collectors.groupingBy(InvoiceApplyLine::getApplyHeaderId));

        for (InvoiceApplyHeader header : savedHeaders) {
            //Recalculate all of amount
            BigDecimal newTotalAmount = BigDecimal.ZERO;
            BigDecimal newExcludeTaxAmount = BigDecimal.ZERO;
            BigDecimal newTaxAmount = BigDecimal.ZERO;

            //Search Lines by headerId and set Amount from each line
            List<InvoiceApplyLine> updatedLines = linesGroupedByHeaderId.get(header.getApplyHeaderId());
            for (InvoiceApplyLine line : updatedLines) {
                newTotalAmount = newTotalAmount.add(line.getTotalAmount() != null ? line.getTotalAmount() : BigDecimal.ZERO);
                newExcludeTaxAmount = newExcludeTaxAmount.add(line.getExcludeTaxAmount() != null ? line.getExcludeTaxAmount() : BigDecimal.ZERO);
                newTaxAmount = newTaxAmount.add(line.getTaxAmount() != null ? line.getTaxAmount() : BigDecimal.ZERO);
            }

            //Set Amount from calculation of Amount
            header.setTotalAmount(newTotalAmount);
            header.setExcludeTaxAmount(newExcludeTaxAmount);
            header.setTaxAmount(newTaxAmount);
        }

        headerRepository.batchUpdateOptional(savedHeaders,
                InvoiceApplyHeader.FIELD_TOTAL_AMOUNT,
                InvoiceApplyHeader.FIELD_EXCLUDE_TAX_AMOUNT,
                InvoiceApplyHeader.FIELD_TAX_AMOUNT);
    }
}

