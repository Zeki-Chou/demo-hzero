package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.dto.InvoiceApplyLineDTO;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
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

    @Autowired
    private InvoiceApplyHeaderService invoiceApplyHeaderService;

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
        invoiceApplyHeaderService.countApplyLineUpdateHeader(headerId);
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

//        Hashset it used to get all header_id that have update or insert
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

//            this code is to get header id if the header id is updated which it will get the old header_id and add it to hashset
//            so there is 2 header_id that need to be updated and calculated
            if(invoiceApplyLine.getApplyHeaderId() != invoiceApplyLine1.getApplyHeaderId()) {
                headerIdSet.add(invoiceApplyLine1.getApplyHeaderId());
            }

            headerIdSet.add(invoiceApplyLine.getApplyHeaderId());
        }

        invoiceApplyLineRepository.batchInsertSelective(insertList);
        invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);

        List<Long> headerIdList = new ArrayList<>(headerIdSet);

        for(int i = 0; i < headerIdList.size(); i++) {
            invoiceApplyHeaderService.countApplyLineUpdateHeader(headerIdList.get(i));
        }
    }
}

