package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.controller.dto.InvoiceApplyHeaderDTO;
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

    public InvoiceApplyHeaderServiceImpl(InvoiceApplyHeaderRepository invoiceApplyHeaderRepository, InvoiceApplyLineRepository invoiceApplyLineRepository) {
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
        this.invoiceApplyLineRepository = invoiceApplyLineRepository;
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
    public void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders, Long organizationId) {

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

        // key is the template header number
        Map<String, List<InvoiceApplyLine>> applyLineList = new HashMap<>();

        for (InvoiceApplyHeaderDTO header: invoiceApplyHeaders) {
            if (!invoiceTypeList.contains(header.getInvoiceType())) {
                throw new CommonException("invalid invoice type");
            }

            if (!invoiceColorList.contains(header.getInvoiceColor())) {
                throw new CommonException("invalid invoice color");
            }

            if (!applyStatusList.contains(header.getApplyStatus())) {
                throw new CommonException("invalid apply status");
            }

            List<InvoiceApplyLine> invoiceApplyData = new ArrayList<>();
            List<InvoiceApplyLine> lineFromRequest = header.getDataList();

            if (lineFromRequest != null) {
                invoiceApplyData.addAll(lineFromRequest);
            }

            if (header.getApplyHeaderId() == null) {
                header.setApplyHeaderNumber(InvoiceApplyHeaderUtils.generateTemplateCode(codeRuleBuilder));
            }

            List<InvoiceApplyLine> applyDataDB = new ArrayList<>();

            if (header.getApplyHeaderId() != null) {
                InvoiceApplyLine applyLineRecord = new InvoiceApplyLine();
                applyLineRecord.setApplyHeaderId(header.getApplyHeaderId());
                applyDataDB.addAll(invoiceApplyLineRepository.selectList(applyLineRecord));
            }

            Utils.addAmountFromLineList(invoiceApplyData, header, applyDataDB);
            applyLineList.put(header.getApplyHeaderNumber(), invoiceApplyData);
        }

        List<InvoiceApplyHeader> insertList = invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() == null).collect(Collectors.toList());
        List<InvoiceApplyHeader> updateList = invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() != null).collect(Collectors.toList());

        List<InvoiceApplyHeader> insertRes = invoiceApplyHeaderRepository.batchInsertSelective(insertList);
        List<InvoiceApplyHeader> updateRes = invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateList);

        // if updated, then remove the cache
        // since data can be changed
        updateRes.forEach(header -> {
            redis.setCurrentDatabase(13);
            String cacheName = header.getApplyHeaderId() + "-applyheader-47359";
            redis.delKey(cacheName);
        });

        List<InvoiceApplyLine> insertApplyLines = new ArrayList<>();
        List<InvoiceApplyLine> updateApplyLines = new ArrayList<>();

        // insert need to get the header id and add it to their lines
        insertRes.forEach(header -> {
            Long headerId = header.getApplyHeaderId();
            List<InvoiceApplyLine> lineList = applyLineList.get(header.getApplyHeaderNumber());
            lineList.forEach(line -> line.setApplyHeaderId(headerId));
            insertApplyLines.addAll(lineList);
        });

        updateRes.forEach(header -> {
            List<InvoiceApplyLine> lineList = applyLineList.get(header.getApplyHeaderNumber());
            updateApplyLines.addAll(lineList);
        });

        invoiceApplyLineRepository.batchInsertSelective(insertApplyLines);
        invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateApplyLines);
    }

    @Override
    public void deleteData(InvoiceApplyHeader invoiceApplyHeader) {
        if (invoiceApplyHeaderRepository.selectByPrimary(invoiceApplyHeader.getApplyHeaderId()) == null) {
            throw new CommonException("header not found");
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

        InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectByPrimary(applyHeaderId);
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
            throw new CommonException("error converting to json string");
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
        String applyStatus = lovAdapter.queryLovMeaning("DEMO-47359.INV_APPLY_HEADER.APPLY_STATUS", organizationId, invoiceApplyHeader.getApplyStatus(), lang);

        // get value set for invoice color
        String invoiceColor = lovAdapter.queryLovMeaning("DEMO-47359.INV_APPLY_HEADER.INV_COLOR", organizationId, invoiceApplyHeader.getInvoiceColor(), lang);

        // get value set for invoice type
        String invoiceType = lovAdapter.queryLovMeaning("DEMO-47359.INV_APPLY_HEADER.INV_TYPE", organizationId, invoiceApplyHeader.getInvoiceType(), lang);

        dto.setApplyStatusMeaning(applyStatus);
        dto.setInvoiceTypeMeaning(invoiceType);
        dto.setInvoiceColorMeaning(invoiceColor);

        return dto;
    }
}

