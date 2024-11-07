package com.hand.demo.app.service.impl;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.ext.IllegalArgumentException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import lombok.AllArgsConstructor;
import org.hzero.mybatis.domian.Condition;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {
        Set<Long> headerIds = invoiceApplyLines.stream()
                .map(InvoiceApplyLine::getApplyHeaderId)
                .collect(Collectors.toSet());

        Condition condition = new Condition(InvoiceApplyHeader.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo("delFlag", 0).andIn("applyHeaderId", headerIds);

        List<Long> existingHeaderIds = invoiceApplyHeaderRepository
                .selectByCondition(condition)
                .stream()
                .map(InvoiceApplyHeader::getApplyHeaderId)
                .collect(Collectors.toList());

        headerIds.removeAll(existingHeaderIds);

        if(!headerIds.isEmpty()) {
            throw new IllegalArgumentException("The following header IDs are missing in the InvoiceApplyHeader table or has been deleted: " + headerIds);
        }

        List<InvoiceApplyLine> saveApplyLines = invoiceApplyLines.stream()
                .peek(line -> {
                    line.setTotalAmount(line.getUnitPrice().multiply(line.getQuantity()));
                    line.setTaxAmount(line.getTotalAmount().multiply(line.getTaxRate()));
                    line.setExcludeTaxAmount(line.getTotalAmount().subtract(line.getTaxAmount()));
                })
                .collect(Collectors.toList());

        List<InvoiceApplyLine> insertList = saveApplyLines.stream().filter(line -> line.getApplyLineId() == null).collect(Collectors.toList());
        List<InvoiceApplyLine> updateList = invoiceApplyLines.stream().filter(line -> line.getApplyLineId() != null).collect(Collectors.toList());

        List<InvoiceApplyLine> savedLines = invoiceApplyLineRepository.batchInsertSelective(insertList);
        if(savedLines != null && !savedLines.isEmpty()) {
            List<Long> savedLinesHeaderIds = savedLines.stream().map(InvoiceApplyLine::getApplyHeaderId).collect(Collectors.toList());

            Condition invoiceHeaderCondition = new Condition(InvoiceApplyHeader.class);
            Condition.Criteria invoiceHeaderCriteria = invoiceHeaderCondition.createCriteria();
            invoiceHeaderCriteria.andIn("applyHeaderId", savedLinesHeaderIds);

            Map<Long, List<InvoiceApplyLine>> applyLineByHeaderId = savedLines.stream()
                    .collect(Collectors.groupingBy(InvoiceApplyLine::getApplyHeaderId));

            List<InvoiceApplyHeader> headersInSavedLines = invoiceApplyHeaderRepository.selectByCondition(invoiceHeaderCondition);
            if(headersInSavedLines != null && !headersInSavedLines.isEmpty()) {
                headersInSavedLines.forEach(header -> {
                    List<InvoiceApplyLine> headerLines = applyLineByHeaderId.get(header.getApplyHeaderId());
                    BigDecimal headerTotalAmount = header.getTotalAmount();
                    BigDecimal headerTaxAmount = header.getTaxAmount();
                    BigDecimal headerExcludeTaxAmount = header.getExcludeTaxAmount();

                    headerLines.forEach(line -> {
                        header.setTotalAmount(headerTotalAmount.add(line.getTotalAmount()));
                        header.setTaxAmount(headerTaxAmount.add(line.getTaxAmount()));
                        header.setExcludeTaxAmount(headerExcludeTaxAmount.add(line.getExcludeTaxAmount()));
                    });

                    invoiceApplyHeaderRepository.updateByPrimaryKeySelective(header);
                });
            }
        }


        List<InvoiceApplyLine> updatedLines = invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);
        if(updatedLines != null && !updatedLines.isEmpty()) {
            List<Long> updatedLinesHeaderIds = updatedLines.stream().map(InvoiceApplyLine::getApplyHeaderId).collect(Collectors.toList());

            Condition updatedInvoiceHeaderCondition = new Condition(InvoiceApplyHeader.class);
            Condition.Criteria updatedInvoiceHeaderCriteria = updatedInvoiceHeaderCondition.createCriteria();
            updatedInvoiceHeaderCriteria.andIn("applyHeaderId", updatedLinesHeaderIds);

            Map<Long, List<InvoiceApplyLine>> updatedApplyLineByHeaderId = updatedLines.stream()
                    .collect(Collectors.groupingBy(InvoiceApplyLine::getApplyHeaderId));

            List<InvoiceApplyHeader> headersInUpdatedLines = invoiceApplyHeaderRepository.selectByCondition(updatedInvoiceHeaderCondition);
            if(headersInUpdatedLines != null && !headersInUpdatedLines.isEmpty()) {
                headersInUpdatedLines.forEach(header -> {
                    List<InvoiceApplyLine> headerLines = updatedApplyLineByHeaderId.get(header.getApplyHeaderId());
                    BigDecimal headerTotalAmount = header.getTotalAmount();
                    BigDecimal headerTaxAmount = header.getTaxAmount();
                    BigDecimal headerExcludeTaxAmount = header.getExcludeTaxAmount();

                    headerLines.forEach(line -> {
                        header.setTotalAmount(headerTotalAmount.add(line.getTotalAmount().subtract(headerTotalAmount)));
                        header.setTaxAmount(headerTaxAmount.add(line.getTaxAmount().subtract(headerTaxAmount)));
                        header.setExcludeTaxAmount(headerExcludeTaxAmount.add(line.getExcludeTaxAmount().subtract(headerExcludeTaxAmount)));
                    });

                    invoiceApplyHeaderRepository.updateByPrimaryKeySelective(header);
                });
            }
        }
    }

    @Override
    public void remove(List<InvoiceApplyLine> invoiceApplyLines) {
        List<Long> headersIDsInApplyLines = invoiceApplyLines.stream().map(InvoiceApplyLine::getApplyHeaderId).collect(Collectors.toList());
        Map<Long, List<InvoiceApplyLine>> applyLinesByHeaderId = invoiceApplyLines.stream()
                .collect(Collectors.groupingBy(InvoiceApplyLine::getApplyHeaderId));

        Condition condition = new Condition(InvoiceApplyHeader.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andIn("applyHeaderId", headersIDsInApplyLines);
        List<InvoiceApplyHeader> headers = invoiceApplyHeaderRepository.selectByCondition(condition);

        headers.forEach(header -> {
            List<InvoiceApplyLine> headerLines = applyLinesByHeaderId.get(header.getApplyHeaderId());
            BigDecimal headerTotalAmount = header.getTotalAmount();
            BigDecimal headerTaxAmount = header.getTaxAmount();
            BigDecimal headerExcludeTaxAmount = header.getExcludeTaxAmount();

            headerLines.forEach(headerLine -> {
                header.setTotalAmount(headerTotalAmount.subtract(headerLine.getTotalAmount()));
                header.setTaxAmount(headerTaxAmount.subtract(headerLine.getTaxAmount()));
                header.setExcludeTaxAmount(headerTaxAmount.subtract(headerLine.getExcludeTaxAmount()));
            });
        });

        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(headers);

        invoiceApplyLineRepository.batchDeleteByPrimaryKey(invoiceApplyLines);
    }
}

