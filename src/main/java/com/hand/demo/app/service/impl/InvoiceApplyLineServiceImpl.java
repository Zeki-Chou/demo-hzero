package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.dto.InvoiceApplyLineDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.hzero.mybatis.domian.Condition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyLine)应用服务
 *
 * @author Fatih Khoiri
 * @since 2024-11-04 10:14:34
 */
@Service
public class InvoiceApplyLineServiceImpl implements InvoiceApplyLineService {
    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private RedisHelper redisHelper;

    @Override
    public Page<InvoiceApplyLine> selectList(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {
        return PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyLineRepository.selectList(invoiceApplyLine));
    }

    @Override
    public Page<InvoiceApplyLineDTO> selectListExcel(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {
        Page<InvoiceApplyLineDTO> pageResult = PageHelper.doPageAndSort(pageRequest,
                () -> invoiceApplyLineRepository.selectList(invoiceApplyLine));
        List<InvoiceApplyLineDTO> invoiceApplyLineDTOS = new ArrayList<>();


        // Change to DTO and get the header number based on header id
        for (int i = 0; i < pageResult.size(); i++) {
            InvoiceApplyLineDTO dataDto = new InvoiceApplyLineDTO();
            BeanUtils.copyProperties(pageResult.get(i), dataDto);
            String headerNumber = invoiceApplyHeaderRepository.selectByPrimary(dataDto.getApplyHeaderId()).getApplyHeaderNumber();
            dataDto.setHeaderNumber(headerNumber);
            invoiceApplyLineDTOS.add(dataDto);
        }

        Page<InvoiceApplyLineDTO> dtoPage = new Page<>();
        dtoPage.setContent(invoiceApplyLineDTOS);
        dtoPage.setTotalPages(pageResult.getTotalPages());
        dtoPage.setTotalElements(pageResult.getTotalElements());
        dtoPage.setNumber(pageResult.getNumber());
        dtoPage.setSize(pageResult.getSize());

        return dtoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {
        List<InvoiceApplyLine> insertList = filterInsertList(invoiceApplyLines);
        List<InvoiceApplyLine> updateList = filterUpdateList(invoiceApplyLines);

        calculateAmountsForLines(insertList);
        if (!insertList.isEmpty()) {
            invoiceApplyLineRepository.batchInsertSelective(insertList);
        }

        calculateAmountsForLines(updateList);
        if (!updateList.isEmpty()) {
            invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);
        }

        updateAffectedHeaders(invoiceApplyLines);
    }

    private List<InvoiceApplyLine> filterInsertList(List<InvoiceApplyLine> lines) {
        return lines.stream()
                .filter(line -> line.getApplyLineId() == null)
                .collect(Collectors.toList());
    }

    private List<InvoiceApplyLine> filterUpdateList(List<InvoiceApplyLine> lines) {
        return lines.stream()
                .filter(line -> line.getApplyLineId() != null)
                .collect(Collectors.toList());
    }

    private void calculateAmountsForLines(List<InvoiceApplyLine> lines) {
        for (InvoiceApplyLine line : lines) {
            line.setTotalAmount(line.getUnitPrice().multiply(line.getQuantity()));
            line.setTaxAmount(line.getTotalAmount().multiply(line.getTaxRate()));
            line.setExcludeTaxAmount(line.getTotalAmount().subtract(line.getTaxAmount()));
        }
    }

    // Get the ids header that affected and then update the amount
    private void updateAffectedHeaders(List<InvoiceApplyLine> invoiceApplyLines) {
        Set<Long> affectedHeaderIds = invoiceApplyLines.stream()
                .map(InvoiceApplyLine::getApplyHeaderId)
                .collect(Collectors.toSet());

        for (Long headerId : affectedHeaderIds) {
            updateHeaderAmounts(headerId);
        }
    }

    // logic to calculate the header amount
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

            redisHelper.delKey(headerData.getApplyHeaderNumber());
            invoiceApplyHeaderRepository.updateByPrimaryKey(headerData);
        }
    }

    // Get the line based on header id
    private List<InvoiceApplyLine> getAssociatedLines(Long applyHeaderId) {
        Condition condition = new Condition(InvoiceApplyHeader.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo("applyHeaderId", applyHeaderId);
        return invoiceApplyLineRepository.selectByCondition(condition);
    }

    // calculate the total amount
    private BigDecimal calculateTotalAmount(List<InvoiceApplyLine> lines, Function<InvoiceApplyLine, BigDecimal> mapper) {
        return lines.stream()
                .map(mapper)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // delete the lines and also update the header that related
    @Override
    public List<InvoiceApplyHeaderDTO> delete(List<InvoiceApplyLine> invoiceApplyLines) {
        List<InvoiceApplyHeaderDTO> headerDTOList = new ArrayList<>();

        for (InvoiceApplyLine line : invoiceApplyLines) {
            Long headerId = line.getApplyHeaderId();
            InvoiceApplyHeader headerData = invoiceApplyHeaderRepository.selectByPrimaryKey(headerId);

            if (headerData != null) {
                processLineDeletion(line, headerData, headerDTOList);
            }
        }

        return headerDTOList;
    }

    // process line to delete the line
    private void processLineDeletion(InvoiceApplyLine line, InvoiceApplyHeader headerData, List<InvoiceApplyHeaderDTO> headerDTOList) {
        InvoiceApplyHeaderDTO headerDTO = changeToDTO(headerData);

        invoiceApplyLineRepository.delete(line);
        List<InvoiceApplyLine> associatedLines = fetchAssociatedLines(headerData.getApplyHeaderId());

        headerDTO.setInvoiceApplyLines(associatedLines);

        updateHeaderAmounts(headerData, associatedLines);

        invoiceApplyHeaderRepository.updateByPrimaryKey(headerData);
        headerDTOList.add(changeToDTO(headerData));
    }

    private List<InvoiceApplyLine> fetchAssociatedLines(Long headerId) {
        return invoiceApplyLineRepository.select("applyHeaderId", headerId);
    }

    // update the amount of header
    private void updateHeaderAmounts(InvoiceApplyHeader headerData, List<InvoiceApplyLine> associatedLines) {
        BigDecimal totalAmount = calculateTotalAmount(associatedLines, InvoiceApplyLine::getTotalAmount);
        BigDecimal excludeTaxAmount = calculateTotalAmount(associatedLines, InvoiceApplyLine::getExcludeTaxAmount);
        BigDecimal taxAmount = calculateTotalAmount(associatedLines, InvoiceApplyLine::getTaxAmount);

        headerData.setTotalAmount(totalAmount);
        headerData.setExcludeTaxAmount(excludeTaxAmount);
        headerData.setTaxAmount(taxAmount);
    }


    private InvoiceApplyHeaderDTO changeToDTO(InvoiceApplyHeader invoiceApplyHeaderDTO) {
        InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();
        BeanUtils.copyProperties(invoiceApplyHeaderDTO, dto);
        return dto;
    }

}

