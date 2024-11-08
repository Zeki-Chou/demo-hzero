package com.hand.demo.app.service.impl;

import com.hand.demo.app.service.InvoiceApplyHeaderService;
import com.hand.demo.domain.dto.InvoiceApplyLineDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
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

    @Override
    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {
        for(InvoiceApplyLine invoiceApplyLine : invoiceApplyLines){
            InvoiceApplyHeader header = headerService.selectById(invoiceApplyLine.getApplyHeaderId());
            if(header != null){
                if(header.getDelFlag() == 0){
                    invoiceApplyLine.setTotalAmount(invoiceApplyLine.getQuantity().multiply(invoiceApplyLine.getUnitPrice()));
                    invoiceApplyLine.setTaxAmount(invoiceApplyLine.getTotalAmount().multiply(invoiceApplyLine.getTaxRate()));
                    invoiceApplyLine.setExcludeTaxAmount(invoiceApplyLine.getTotalAmount().subtract(invoiceApplyLine.getTaxAmount()));

                    if(invoiceApplyLine.getApplyLineId() == null){
                        invoiceApplyLineRepository.insert(invoiceApplyLine);
                        recalculateAndUpdateHeaderTotals(header);
                    }else {
                        invoiceApplyLineRepository.updateByPrimaryKeySelective(invoiceApplyLine);
                        recalculateAndUpdateHeaderTotals(header);
                    }
                }
            }
        }
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

