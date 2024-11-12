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
import org.opensaml.xml.signature.P;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
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
                throw new CommonException("demo-47359.warn.invoice_apply_line.header_not_found", invoiceApplyLine.getApplyHeaderId());
            } else if (findHeader.getDelFlag() == 1) {
                throw new CommonException("demo-47359.warn.invoice_apply_line.header_deleted", invoiceApplyLine.getApplyHeaderId());
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
    public void saveDataTest(List<InvoiceApplyLine> invoiceApplyLines) {
        // validate invoice line
        validateData(invoiceApplyLines);
        // calculate the 3 amount from list of line
        List<InvoiceApplyLine> updatedLineAmounts = invoiceApplyLines.stream().map(Utils::calculateAmountLine).collect(Collectors.toList());

        // insert and update invoice line
        List<InvoiceApplyLine> insertList = updatedLineAmounts.stream().filter(line -> line.getApplyLineId() == null).collect(Collectors.toList());
        List<InvoiceApplyLine> updateList = updatedLineAmounts.stream().filter(line -> line.getApplyLineId() != null).collect(Collectors.toList());

        invoiceApplyLineRepository.batchInsertSelective(insertList);
        invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);

        // recalculate header amount
        List<InvoiceApplyHeader> headersToUpdate = recalculateAmount(invoiceApplyLines);
        List<InvoiceApplyHeader> updatedHeaderAmount = updateHeaderAmount(headersToUpdate);

        // update the header
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updatedHeaderAmount);
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
     * validation requirement:
     * 1. header exist in database
     * 2. header has not been deleted (del_flag)
     * @param invoiceApplyLines invoice line object
     */
    private void validateData(List<InvoiceApplyLine> invoiceApplyLines) {

        String headerIds = generateStringIds(invoiceApplyLines);

        Map<Long, InvoiceApplyHeader> headersMap = invoiceApplyHeaderRepository
                .selectByIds(headerIds)
                .stream()
                .collect(Collectors.toMap(InvoiceApplyHeader::getApplyHeaderId, Function.identity()));

        for (InvoiceApplyLine line: invoiceApplyLines) {
            if (line.getApplyHeaderId() == null) {
                throw new CommonException("invoice header id is null");
            }

            if (!headersMap.containsKey(line.getApplyHeaderId())) {
                throw new CommonException("demo-47359.warn.invoice_apply_line.header_not_found", line.getApplyHeaderId());
            }

            InvoiceApplyHeader headerTest = headersMap.get(line.getApplyHeaderId());
            if (headerTest.getDelFlag() != 0) {
                throw  new CommonException(("invoice header has been deleted"));
            }
        }
    }

    /**
     * find invoice headers that needs to recalculate reset headers amount to zero
     * @param invoiceApplyLines list of apply lines
     * @return list of invoice headers with reset amount that needs to be calculated
     */
    private List<InvoiceApplyHeader> recalculateAmount(List<InvoiceApplyLine> invoiceApplyLines) {
        String headerIds = generateStringIds(invoiceApplyLines);
        List<InvoiceApplyHeader> headers = invoiceApplyHeaderRepository.selectByIds(headerIds);
        headers.forEach(header -> {
            header.setTotalAmount(BigDecimal.ZERO);
            header.setTaxAmount(BigDecimal.ZERO);
            header.setExcludeTaxAmount(BigDecimal.ZERO);
        });
        return headers;
    }

    private List<InvoiceApplyHeader> updateHeaderAmount(List<InvoiceApplyHeader> headers) {
        // get lines related to their corresponding header id
        List<Long> headerIds = headers.stream().map(InvoiceApplyHeader::getApplyHeaderId).collect(Collectors.toList());

        if (headerIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineRepository.selectByHeaderIds(headerIds);
        Map<Long, InvoiceApplyHeader> headersMap = headers
                .stream()
                .collect(Collectors.toMap(InvoiceApplyHeader::getApplyHeaderId, Function.identity()));
        invoiceApplyLines.forEach(line -> {
            InvoiceApplyHeader header = headersMap.get(line.getApplyHeaderId());
            BigDecimal totalAmount = header.getTotalAmount().add(line.getTotalAmount());
            BigDecimal taxAmount = header.getTaxAmount().add(line.getTaxAmount());
            BigDecimal excludeTaxAmount = header.getExcludeTaxAmount().add(line.getExcludeTaxAmount());

            header.setTotalAmount(totalAmount);
            header.setTaxAmount(taxAmount);
            header.setExcludeTaxAmount(excludeTaxAmount);

            headersMap.put(header.getApplyHeaderId(), header);
        });

        return new ArrayList<>(headersMap.values());
    }

    /**
     * generate a string that contains joined ids separated by commas
     * @param invoiceApplyLines list of invoice line object
     * @return string of ids
     */
    private String generateStringIds(List<InvoiceApplyLine> invoiceApplyLines) {
        List<String> headerIds = invoiceApplyLines
                .stream()
                .filter(line -> line.getApplyHeaderId() != null)
                .map(line -> String.valueOf(line.getApplyHeaderId()))
                .collect(Collectors.toList());

        return String.join(",", headerIds);
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

