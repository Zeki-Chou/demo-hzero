package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.controller.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.InvApplyHeaderConstant;
import com.hand.demo.infra.mapper.InvoiceApplyHeaderMapper;
import com.hand.demo.infra.util.InvoiceApplyHeaderUtils;
import com.hand.demo.infra.util.Utils;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.message.MessageAccessor;
import org.hzero.core.redis.RedisHelper;
import org.hzero.mybatis.common.Criteria;
import org.hzero.mybatis.common.query.Comparison;
import org.hzero.mybatis.common.query.WhereField;
import org.hzero.mybatis.domian.Condition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author
 * @since 2024-11-04 14:40:36
 */
@Service
public class InvoiceApplyHeaderServiceImpl implements InvoiceApplyHeaderService {
    @Autowired
    private LovAdapter lovAdapter;

    @Autowired
    private CodeRuleBuilder codeRuleBuilder;

    @Autowired
    private InvoiceApplyHeaderMapper mapper;

    @Autowired
    private RedisHelper redis;

    @Autowired
    private ObjectMapper objectMapper;

    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    private final InvoiceApplyLineRepository invoiceApplyLineRepository;

    private final InvoiceApplyLineService invoiceApplyLineService;

    public InvoiceApplyHeaderServiceImpl(InvoiceApplyHeaderRepository invoiceApplyHeaderRepository, InvoiceApplyLineRepository invoiceApplyLineRepository, InvoiceApplyLineService invoiceApplyLineService) {
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
        this.invoiceApplyLineRepository = invoiceApplyLineRepository;
        this.invoiceApplyLineService = invoiceApplyLineService;
    }

    @Override
    public Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader, Long organizationId) {
        if (invoiceApplyHeader.getDelFlag() == null) {
            invoiceApplyHeader.setDelFlag(0);
        }

        Page<InvoiceApplyHeader> pageList = PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyHeaderRepository.selectList(invoiceApplyHeader));
        List<InvoiceApplyHeaderDTO> headerDTOS = new ArrayList<>();
        for (InvoiceApplyHeader data : pageList) {
            headerDTOS.add(mapToDto(data, organizationId));
        }

        Page<InvoiceApplyHeaderDTO> dtoPage = new Page<>();
        dtoPage.setContent(headerDTOS);
        dtoPage.setTotalPages(pageList.getTotalPages());
        dtoPage.setTotalElements(pageList.getTotalElements());
        dtoPage.setNumber(pageList.getNumber());
        dtoPage.setSize(pageList.getSize());

        return dtoPage;
    }

    @Override
    @Transactional
    public void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders, Long organizationId) {
        // validate
        validateHeader(invoiceApplyHeaders, organizationId);
        Map<String, List<InvoiceApplyLine>> lineListMap = new HashMap<>();
        List<InvoiceApplyLine> lineWithHeaderIdList = new ArrayList<>();

        // put list of invoice lines into map
        invoiceApplyHeaders.forEach(header -> {
            String templateCode = InvoiceApplyHeaderUtils.generateTemplateCode(codeRuleBuilder);
            header.setApplyHeaderNumber(templateCode);

            header.setTotalAmount(BigDecimal.ZERO);
            header.setTaxAmount(BigDecimal.ZERO);
            header.setExcludeTaxAmount(BigDecimal.ZERO);

            lineListMap.put(templateCode, header.getDataList());
        });

        //update and insert new headers
        List<InvoiceApplyHeader> insertList = invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() == null).collect(Collectors.toList());
        List<InvoiceApplyHeader> updateList = invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() != null).collect(Collectors.toList());

        List<InvoiceApplyHeader> insertRes = invoiceApplyHeaderRepository.batchInsertSelective(insertList);
        List<InvoiceApplyHeader> updateRes = invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateList);

        // set the new apply header id into invoice line if invoice header is new
        for (InvoiceApplyHeader header: insertRes) {
            List<InvoiceApplyLine> invoiceApplyLineList = lineListMap.getOrDefault(header.getApplyHeaderNumber(), new ArrayList<>());
            invoiceApplyLineList.forEach(line -> line.setApplyHeaderId(header.getApplyHeaderId()));
            lineWithHeaderIdList.addAll(invoiceApplyLineList);
        }

        for (InvoiceApplyHeader header: updateRes) {
            List<InvoiceApplyLine> invoiceApplyLineList = lineListMap.get(header.getApplyHeaderNumber());
            lineWithHeaderIdList.addAll(invoiceApplyLineList);

            // delete cache since header data may change
            redis.setCurrentDatabase(13);
            String cacheName = header.getApplyHeaderId() + "-applyheader-47359";
            redis.delKey(cacheName);
        }

        if (!lineWithHeaderIdList.isEmpty()) {
            invoiceApplyLineService.saveData(lineWithHeaderIdList);
        }

    }

    @Override
    @Transactional
    public void saveDataTest(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders, Long organizationId) {
        // validate
        validateHeader(invoiceApplyHeaders, organizationId);
        Map<String, List<InvoiceApplyLine>> lineListMap = new HashMap<>();
        List<InvoiceApplyLine> lineWithHeaderIdList = new ArrayList<>();

        // put list of invoice lines into map
        invoiceApplyHeaders.forEach(header -> {
            String templateCode = InvoiceApplyHeaderUtils.generateTemplateCode(codeRuleBuilder);
            header.setApplyHeaderNumber(templateCode);

            header.setTotalAmount(BigDecimal.ZERO);
            header.setTaxAmount(BigDecimal.ZERO);
            header.setExcludeTaxAmount(BigDecimal.ZERO);

            lineListMap.put(templateCode, header.getDataList());
        });

        //update and insert new headers
        List<InvoiceApplyHeader> insertList = invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() == null).collect(Collectors.toList());
        List<InvoiceApplyHeader> updateList = invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() != null).collect(Collectors.toList());

        List<InvoiceApplyHeader> insertRes = invoiceApplyHeaderRepository.batchInsertSelective(insertList);
        List<InvoiceApplyHeader> updateRes = invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateList);

        // set the new apply header id into invoice line if invoice header is new
        for (InvoiceApplyHeader header: insertRes) {
            List<InvoiceApplyLine> invoiceApplyLineList = lineListMap.getOrDefault(header.getApplyHeaderNumber(), new ArrayList<>());
            invoiceApplyLineList.forEach(line -> line.setApplyHeaderId(header.getApplyHeaderId()));
            lineWithHeaderIdList.addAll(invoiceApplyLineList);
        }

        for (InvoiceApplyHeader header: updateRes) {
            List<InvoiceApplyLine> invoiceApplyLineList = lineListMap.get(header.getApplyHeaderNumber());
            lineWithHeaderIdList.addAll(invoiceApplyLineList);

            // delete cache since header data may change
            redis.setCurrentDatabase(13);
            String cacheName = header.getApplyHeaderId() + "-applyheader-47359";
            redis.delKey(cacheName);
        }

        if (!lineWithHeaderIdList.isEmpty()) {
            invoiceApplyLineService.saveDataTest(lineWithHeaderIdList);
        }

    }

    @Override
    public void deleteData(InvoiceApplyHeader invoiceApplyHeader) {
        if (invoiceApplyHeaderRepository.selectByPrimary(invoiceApplyHeader.getApplyHeaderId()) == null) {
            throw new CommonException("demo-47359.warn.invoice_apply_line.not_found", invoiceApplyHeader.getApplyHeaderId());
        }
        mapper.updateDelFlag(invoiceApplyHeader);
    }

    @Override
    public InvoiceApplyHeaderDTO detail(Long applyHeaderId) {
        String cacheName = applyHeaderId + "-applyheader-47359";
        redis.setCurrentDatabase(13);

        if (redis.hasKey(cacheName)) {
            return redis.strGet(cacheName, InvoiceApplyHeaderDTO.class);
        }

        InvoiceApplyHeaderDTO header = invoiceApplyHeaderRepository.selectByPrimary(applyHeaderId);
        if (header == null) {
            throw new CommonException("demo-47359.warn.invoice_apply_line.not_found", applyHeaderId);
        }

        InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();
        BeanUtils.copyProperties(header, dto);
        InvoiceApplyLine invoiceApplyLine = new InvoiceApplyLine();
        invoiceApplyLine.setApplyHeaderId(applyHeaderId);
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineRepository.selectList(invoiceApplyLine);
        dto.setDataList(invoiceApplyLines);

        try {
            String jsonStringDto = objectMapper.writeValueAsString(dto);
            redis.strSet(cacheName, jsonStringDto);
        } catch (JsonProcessingException e) {
            throw new CommonException("demo-47359.error.object_mapper.json_context");
        }

        return dto;
    }

    @Override
    public List<InvoiceApplyHeaderDTO> exportAll(Long organizationId) {
        List<InvoiceApplyHeader> headers = invoiceApplyHeaderRepository.selectAll();
        List<InvoiceApplyHeaderDTO> headerDTOS = new ArrayList<>();

        for (InvoiceApplyHeader header : headers) {
            headerDTOS.add(mapToDto(header, organizationId));
        }

        return headerDTOS;
    }

    /**
     * transform invoiceApplyHeader to appropriate DTO object
     * @param invoiceApplyHeader invoice apply header object
     * @param organizationId tenant id
     * @return invoiceApplyHeaderDTO
     */
    private InvoiceApplyHeaderDTO mapToDto(InvoiceApplyHeader invoiceApplyHeader, Long organizationId) {

        InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();
        BeanUtils.copyProperties(invoiceApplyHeader, dto);

        String lang = "zh_CN";

        // get value set for apply status
        String applyStatus = lovAdapter.queryLovMeaning(InvApplyHeaderConstant.APPLY_STATUS, organizationId, invoiceApplyHeader.getApplyStatus(), lang);

        // get value set for invoice color
        String invoiceColor = lovAdapter.queryLovMeaning(InvApplyHeaderConstant.INVOICE_COLOR, organizationId, invoiceApplyHeader.getInvoiceColor(), lang);

        // get value set for invoice type
        String invoiceType = lovAdapter.queryLovMeaning(InvApplyHeaderConstant.INVOICE_TYPE, organizationId, invoiceApplyHeader.getInvoiceType(), lang);

        dto.setApplyStatusMeaning(applyStatus);
        dto.setInvoiceTypeMeaning(invoiceType);
        dto.setInvoiceColorMeaning(invoiceColor);

        return dto;
    }

    /**
     * check valid apply status, invoice color and invoice type
     * @param dtoList list of apply header dto
     * @param organizationId tenant id
     */
    private void validateHeader(List<InvoiceApplyHeaderDTO> dtoList, Long organizationId) {
        List<String> applyStatusList = lovAdapter
                .queryLovValue(InvApplyHeaderConstant.APPLY_STATUS, organizationId)
                .stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        List<String> invoiceColorList = lovAdapter
                .queryLovValue(InvApplyHeaderConstant.INVOICE_COLOR, organizationId)
                .stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        List<String> invoiceTypeList = lovAdapter
                .queryLovValue(InvApplyHeaderConstant.INVOICE_TYPE, organizationId)
                .stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        for (InvoiceApplyHeaderDTO header: dtoList) {
            if (!invoiceTypeList.contains(header.getInvoiceType())) {
                throw new CommonException("demo-47359.warn.invoice_apply_header.invoice_type", header.getInvoiceType());
            }

            if (!invoiceColorList.contains(header.getInvoiceColor())) {
                throw new CommonException("demo-47359.warn.invoice_apply_header.invoice_color", header.getInvoiceType());
            }

            if (!applyStatusList.contains(header.getApplyStatus())) {
                throw new CommonException("demo-47359.warn.invoice_apply_header.apply_status", header.getApplyStatus());
            }
        }
    }
}

