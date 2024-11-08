package com.hand.demo.app.service.impl;

import com.hand.demo.api.controller.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.controller.dto.InvoiceApplyLineDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.util.Utils;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyLine)应用服务
 *
 * @author Allan
 * @since 2024-11-04 11:21:14
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
    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {

        Map<Long, InvoiceApplyHeader> invoiceApplyHeaderMap = new HashMap<>();

        for (InvoiceApplyLine invoiceApplyLine: invoiceApplyLines) {

            InvoiceApplyHeader findHeader;

            if (invoiceApplyHeaderMap.containsKey(invoiceApplyLine.getApplyHeaderId())) {
                findHeader = invoiceApplyHeaderMap.get(invoiceApplyLine.getApplyHeaderId());
            } else {
                findHeader = invoiceApplyHeaderRepository.selectByPrimary(
                        invoiceApplyLine.getApplyHeaderId()
                );
            }

            if (findHeader == null) {
                throw new CommonException("invoice apply header not found");
            } else if (findHeader.getDelFlag() == 1) {
                throw new CommonException("invoice apply header has been deleted");
            }

            // calculate invoice apply line amounts
            BigDecimal lineTotalAmount = invoiceApplyLine.getUnitPrice().multiply(invoiceApplyLine.getQuantity());
            BigDecimal lineTaxAmount = lineTotalAmount.multiply(invoiceApplyLine.getTaxRate());
            BigDecimal lineExcludeTaxAmount = lineTotalAmount.subtract(lineTaxAmount);

            invoiceApplyLine.setExcludeTaxAmount(lineExcludeTaxAmount);
            invoiceApplyLine.setTaxAmount(lineTaxAmount);
            invoiceApplyLine.setTotalAmount(lineTotalAmount);

            BigDecimal headerTotalAmount = findHeader.getTotalAmount();
            BigDecimal headerTaxAmount = findHeader.getTaxAmount();
            BigDecimal headerExcludeTaxAmount = findHeader.getExcludeTaxAmount();

            // update header amount
            if (invoiceApplyLine.getApplyLineId() == null) {
                // if line is new, it should only add the number into the header
                BigDecimal sumHeaderTotalAmount = headerTotalAmount.add(lineTotalAmount);
                BigDecimal sumHeaderTaxAmount = headerTaxAmount.add(lineTaxAmount);
                BigDecimal sumHeaderExcludeTaxAmount = headerExcludeTaxAmount.add(lineExcludeTaxAmount);

                findHeader.setTotalAmount(sumHeaderTotalAmount);
                findHeader.setExcludeTaxAmount(sumHeaderExcludeTaxAmount);
                findHeader.setTaxAmount(sumHeaderTaxAmount);
            } else {
                InvoiceApplyLine prev = invoiceApplyLineRepository.selectByPrimary(invoiceApplyLine.getApplyLineId());

                // if update line, get amount difference between the previous
                // and add it to amount in header
                InvoiceApplyLine applyLineDiff = Utils.invoiceApplyLineDiff(invoiceApplyLine, prev);

                BigDecimal sumHeaderTotalAmount = headerTotalAmount.add(applyLineDiff.getTotalAmount());
                BigDecimal sumHeaderTaxAmount = headerTaxAmount.add(applyLineDiff.getTaxAmount());
                BigDecimal sumHeaderExcludeTaxAmount = headerExcludeTaxAmount.add(applyLineDiff.getExcludeTaxAmount());

                findHeader.setTotalAmount(sumHeaderTotalAmount);
                findHeader.setExcludeTaxAmount(sumHeaderTaxAmount);
                findHeader.setTaxAmount(sumHeaderExcludeTaxAmount);
            }

            // cache into hash map
            invoiceApplyHeaderMap.put(findHeader.getApplyHeaderId(), findHeader);
            invoiceApplyHeaderRepository.updateByPrimaryKeySelective(findHeader);
        }

        List<InvoiceApplyLine> insertList = invoiceApplyLines.stream().filter(line -> line.getApplyLineId() == null).collect(Collectors.toList());
        List<InvoiceApplyLine> updateList = invoiceApplyLines.stream().filter(line -> line.getApplyLineId() != null).collect(Collectors.toList());
        invoiceApplyLineRepository.batchInsertSelective(insertList);
        invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);
    }

    @Override
    public void deleteApplyLine(List<InvoiceApplyLine> invoiceApplyLines) {

        Map<Long, InvoiceApplyHeader> headerMap = new HashMap<>();
        List<InvoiceApplyLine> linesToDelete = new ArrayList<>();

        invoiceApplyLines.forEach(line -> {

            // assume that only apply line id is required
            // then we need to find the object to check if it exists in db
            InvoiceApplyLine invoiceApplyLine = invoiceApplyLineRepository.selectOne(line);

            if (invoiceApplyLine == null) {
                throw new CommonException("invoice apply line with id " + line.getApplyLineId() + " not exist");
            }

            InvoiceApplyHeader header;

            if (headerMap.containsKey(invoiceApplyLine.getApplyHeaderId())) {
                header = headerMap.get(invoiceApplyLine.getApplyHeaderId());
            } else {
                header = invoiceApplyHeaderRepository.selectByPrimary(invoiceApplyLine.getApplyHeaderId());
                if (header == null) {
                    throw new CommonException("header not found");
                }
            }

            InvoiceApplyLine headerAmount = new InvoiceApplyLine();
            headerAmount.setTaxAmount(header.getTaxAmount());
            headerAmount.setTotalAmount(header.getTotalAmount());
            headerAmount.setExcludeTaxAmount(header.getExcludeTaxAmount());

            InvoiceApplyLine headerAmountDiff = Utils.invoiceApplyLineDiff(headerAmount, invoiceApplyLine);
            header.setTotalAmount(headerAmountDiff.getTotalAmount());
            header.setTaxAmount(headerAmountDiff.getTaxAmount());
            header.setExcludeTaxAmount(headerAmountDiff.getExcludeTaxAmount());

            // cache into hash map
            headerMap.put(header.getApplyHeaderId(), header);
            linesToDelete.add(invoiceApplyLine);
        });

        // update header
        List<InvoiceApplyHeader> headersToUpdate = new ArrayList<>(headerMap.values());
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(headersToUpdate);

        // delete lines
        invoiceApplyLineRepository.batchDeleteByPrimaryKey(linesToDelete);
    }

    @Override
    public List<InvoiceApplyLineDTO> exportAll(InvoiceApplyLineDTO dto) {
        List<InvoiceApplyLine> lineList = invoiceApplyLineRepository.selectList(dto);

        // transform to header id in string value and concat then with "," as delimiter
        String ids = lineList
                .stream()
                .map(line -> String.valueOf(line.getApplyHeaderId()))
                .collect(Collectors.joining(","));

        List<InvoiceApplyHeader> headers = invoiceApplyHeaderRepository.selectByIds(ids);

        // convert from list to map with key header id and value of header number
        Map<Long, String> headersMap = headers
                .stream()
                .collect(
                        Collectors.toMap(
                                InvoiceApplyHeader::getApplyHeaderId,
                                InvoiceApplyHeader::getApplyHeaderNumber
                        )
                );

        return lineList.stream().map(line -> {
            InvoiceApplyLineDTO lineDto = mapToDto(line);
            lineDto.setApplyHeaderNumber(headersMap.get(lineDto.getApplyHeaderId()));
            return lineDto;
        }).collect(Collectors.toList());

    }

    /**
     * convert to dto but header number not being added
     * @param invoiceApplyLine apply line object
     * @return new dto object
     */
    private InvoiceApplyLineDTO mapToDto(InvoiceApplyLine invoiceApplyLine) {
        InvoiceApplyLineDTO dto = new InvoiceApplyLineDTO();
        BeanUtils.copyProperties(invoiceApplyLine, dto);
        return dto;
    }
}

