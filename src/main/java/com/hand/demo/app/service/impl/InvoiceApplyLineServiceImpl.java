package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.dto.InvoiceApplyLineDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.interfaces.ds.strategy.In;
import org.hzero.common.HZeroService;
import org.hzero.core.base.BaseConstants;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyLine)应用服务
 *
 * @author
 * @since 2024-11-05 10:21:26
 */
@Service
public class InvoiceApplyLineServiceImpl implements InvoiceApplyLineService {
    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Override
    public Page<InvoiceApplyLine> selectList(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {
        return PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyLineRepository.selectList(invoiceApplyLine));
    }

    @Override
    public List<InvoiceApplyLineDTO> exportAll(InvoiceApplyLineDTO invoiceApplyLineDTO) {
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineRepository.select(invoiceApplyLineDTO);

        return invoiceApplyLines.stream()
                .map(line -> {
                    InvoiceApplyLineDTO dto = new InvoiceApplyLineDTO();
                    BeanUtils.copyProperties(line, dto);
                    dto.setHeaderNumber(getHeaderNumberByApplyHeaderId(line.getApplyHeaderId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private String getHeaderNumberByApplyHeaderId(Long applyHeaderId) {
        InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectByPrimaryKey(applyHeaderId);
        return header != null ? header.getApplyHeaderNumber() : null;
    }

    @Override
    public void deleteData(Long lineId) {
        InvoiceApplyLine invoiceApplyLine = invoiceApplyLineRepository.selectByPrimary(lineId);
        Long headerId = invoiceApplyLine.getApplyHeaderId();

        invoiceApplyLineRepository.deleteByPrimaryKey(lineId);

        InvoiceApplyLine invoiceApplyLine3 = new InvoiceApplyLine();
        invoiceApplyLine3.setApplyHeaderId(headerId);

        List<InvoiceApplyLine> invoiceApplyLineList = invoiceApplyLineRepository.select(invoiceApplyLine3);

        BigDecimal headerTaxAmount = BigDecimal.ZERO;
        BigDecimal headerExcludeTaxAmount = BigDecimal.ZERO;
        BigDecimal headerTotalAmount = BigDecimal.ZERO;

        for(int i = 0; i < invoiceApplyLineList.size(); i++) {
            InvoiceApplyLine invoiceApplyLine1 = invoiceApplyLineList.get(i);
            BigDecimal taxAmount = invoiceApplyLine1.getTaxAmount() != null ? invoiceApplyLine1.getTaxAmount() : BigDecimal.ZERO;
            BigDecimal excludeTaxAmount = invoiceApplyLine1.getExcludeTaxAmount() != null ? invoiceApplyLine1.getExcludeTaxAmount() : BigDecimal.ZERO;
            BigDecimal totalAmount = invoiceApplyLine1.getTotalAmount() != null ? invoiceApplyLine1.getTotalAmount() : BigDecimal.ZERO;

            headerTaxAmount = headerTaxAmount.add(taxAmount);
            headerExcludeTaxAmount = headerExcludeTaxAmount.add(excludeTaxAmount);
            headerTotalAmount = headerTotalAmount.add(totalAmount);
        }

        InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(headerId);
        invoiceApplyHeader.setApplyHeaderId(headerId);
        invoiceApplyHeader.setTaxAmount(headerTaxAmount);
        invoiceApplyHeader.setExcludeTaxAmount(headerExcludeTaxAmount);
        invoiceApplyHeader.setTotalAmount(headerTotalAmount);

        invoiceApplyHeaderRepository.updateByPrimaryKeySelective(invoiceApplyHeader);
    }

    @Override
    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {
        for(int i = 0; i < invoiceApplyLines.size(); i++) {
            InvoiceApplyLine invoiceApplyLine = invoiceApplyLines.get(i);

            InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(invoiceApplyLine.getApplyHeaderId());

            if(invoiceApplyHeader == null || invoiceApplyHeader.getDelFlag() == BaseConstants.Flag.YES) {
                throw new IllegalArgumentException("Header Id not found or deleted, Please check again !");
            }
        }

        Set<Long> headerIdSet = new HashSet<>();
        List<InvoiceApplyLine> insertList = invoiceApplyLines.stream().filter(line -> line.getApplyLineId() == null).collect(Collectors.toList());
        for(int i = 0; i < insertList.size(); i++) {
            InvoiceApplyLine invoiceApplyLine = insertList.get(i);

            BigDecimal totalAmount = invoiceApplyLine.getUnitPrice().multiply(invoiceApplyLine.getQuantity());
            BigDecimal taxAmount = totalAmount.multiply(invoiceApplyLine.getTaxRate());
            BigDecimal excludeTaxAmount = totalAmount.subtract(taxAmount);

            invoiceApplyLine.setTotalAmount(totalAmount);
            invoiceApplyLine.setTaxAmount(taxAmount);
            invoiceApplyLine.setExcludeTaxAmount(excludeTaxAmount);
            headerIdSet.add(invoiceApplyLine.getApplyHeaderId());
        }

        List<InvoiceApplyLine> updateList = invoiceApplyLines.stream().filter(line -> line.getApplyLineId() != null).collect(Collectors.toList());
        for(int i = 0; i < updateList.size(); i++) {
            InvoiceApplyLine invoiceApplyLine = updateList.get(i);
            InvoiceApplyLine invoiceApplyLine1 = invoiceApplyLineRepository.selectByPrimary(invoiceApplyLine.getApplyLineId());
            invoiceApplyLine.setObjectVersionNumber(invoiceApplyLine1.getObjectVersionNumber());

            BigDecimal totalAmount = invoiceApplyLine.getUnitPrice().multiply(invoiceApplyLine.getQuantity());
            BigDecimal taxAmount = totalAmount.multiply(invoiceApplyLine.getTaxRate());
            BigDecimal excludeTaxAmount = totalAmount.subtract(taxAmount);

            invoiceApplyLine.setTotalAmount(totalAmount);
            invoiceApplyLine.setTaxAmount(taxAmount);
            invoiceApplyLine.setExcludeTaxAmount(excludeTaxAmount);

            headerIdSet.add(invoiceApplyLine.getApplyHeaderId());
        }

        invoiceApplyLineRepository.batchInsertSelective(insertList);
        invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);

        List<Long> headerIdList = new ArrayList<>(headerIdSet);

        for(int i = 0; i < headerIdList.size(); i++) {
            BigDecimal headerTaxAmount = BigDecimal.ZERO;
            BigDecimal headerExcludeTaxAmount = BigDecimal.ZERO;
            BigDecimal headerTotalAmount = BigDecimal.ZERO;

            InvoiceApplyLine invoiceApplyLine = new InvoiceApplyLine();
            invoiceApplyLine.setApplyHeaderId(headerIdList.get(i));

            List<InvoiceApplyLine> invoiceApplyLineList = invoiceApplyLineRepository.select(invoiceApplyLine);
            for(int p = 0; p < invoiceApplyLineList.size(); p++) {
                InvoiceApplyLine invoiceApplyLine1 = invoiceApplyLineList.get(p);

                BigDecimal taxAmount = invoiceApplyLine1.getTaxAmount() != null ? invoiceApplyLine1.getTaxAmount() : BigDecimal.ZERO;
                BigDecimal excludeTaxAmount = invoiceApplyLine1.getExcludeTaxAmount() != null ? invoiceApplyLine1.getExcludeTaxAmount() : BigDecimal.ZERO;
                BigDecimal totalAmount = invoiceApplyLine1.getTotalAmount() != null ? invoiceApplyLine1.getTotalAmount() : BigDecimal.ZERO;

                headerTaxAmount = headerTaxAmount.add(taxAmount);
                headerExcludeTaxAmount = headerExcludeTaxAmount.add(excludeTaxAmount);
                headerTotalAmount = headerTotalAmount.add(totalAmount);
            }

            InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(headerIdList.get(i));
            invoiceApplyHeader.setApplyHeaderId(headerIdList.get(i));
            invoiceApplyHeader.setTaxAmount(headerTaxAmount);
            invoiceApplyHeader.setExcludeTaxAmount(headerExcludeTaxAmount);
            invoiceApplyHeader.setTotalAmount(headerTotalAmount);

            invoiceApplyHeaderRepository.updateByPrimaryKeySelective(invoiceApplyHeader);
        }
    }
}

