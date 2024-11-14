package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.controller.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.BaseConstant;
import com.hand.demo.infra.constant.InvApplyHeaderConstant;
import com.hand.demo.infra.mapper.InvoiceApplyHeaderMapper;
import com.hand.demo.infra.util.InvoiceApplyHeaderUtils;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.apaas.common.userinfo.infra.feign.IamRemoteService;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.redis.RedisHelper;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * @author Allan
 * @since 2024-11-04 14:40:36
 */
@Service
public class InvoiceApplyHeaderServiceImpl implements InvoiceApplyHeaderService {
    private final LovAdapter lovAdapter;

    private final CodeRuleBuilder codeRuleBuilder;

    private final InvoiceApplyHeaderMapper mapper;

    private final RedisHelper redis;

    private final ObjectMapper objectMapper;

    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    private final InvoiceApplyLineRepository invoiceApplyLineRepository;

    private final InvoiceApplyLineService invoiceApplyLineService;

    private final IamRemoteService iamRemoteService;

    public InvoiceApplyHeaderServiceImpl(
            InvoiceApplyLineRepository invoiceApplyLineRepository,
            InvoiceApplyLineService invoiceApplyLineService,
            InvoiceApplyHeaderRepository invoiceApplyHeaderRepository,
            ObjectMapper objectMapper,
            RedisHelper redis,
            InvoiceApplyHeaderMapper mapper,
            CodeRuleBuilder codeRuleBuilder,
            LovAdapter lovAdapter,
            IamRemoteService iamRemoteService
    ) {
        this.invoiceApplyLineRepository = invoiceApplyLineRepository;
        this.invoiceApplyLineService = invoiceApplyLineService;
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
        this.objectMapper = objectMapper;
        this.redis = redis;
        this.mapper = mapper;
        this.codeRuleBuilder = codeRuleBuilder;
        this.lovAdapter = lovAdapter;
        this.iamRemoteService = iamRemoteService;
    }

    @Override
    public Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeaderDTO invoiceApplyHeader, Long organizationId) {

        if (invoiceApplyHeader.getDelFlag() == null) {
            invoiceApplyHeader.setDelFlag(0);
        }

        ResponseEntity<String> iamResponse = iamRemoteService.selectSelf();

        if (iamResponse.getStatusCode() != HttpStatus.OK) {
            throw new CommonException("Error getting iam object");
        }

        String responseJsonString = iamResponse.getBody();
        JSONObject iamJsonString = new JSONObject(responseJsonString);
        Boolean isTenantAdmin = iamJsonString.getBoolean("tenantAdminFlag");
        invoiceApplyHeader.setTenantAdminFlag(isTenantAdmin);
        invoiceApplyHeader.setTenantAdminFlag(false);

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
    @Transactional(rollbackFor = Exception.class)
    public void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders, Long organizationId) {
        // validate
        validateHeader(invoiceApplyHeaders, organizationId);
        Map<String, List<InvoiceApplyLine>> lineListMap = new HashMap<>();
        List<InvoiceApplyLine> lineWithHeaderIdList = new ArrayList<>();

        // put list of invoice lines into map
        invoiceApplyHeaders.forEach(header -> {
            StringBuilder stringBuilder = new StringBuilder();

            if (header.getApplyHeaderNumber() == null) {
                String newTemplateCode = InvoiceApplyHeaderUtils.generateInvoiceCode(codeRuleBuilder);
                stringBuilder.append(newTemplateCode);
            } else {
                stringBuilder.append(header.getApplyHeaderNumber());
            }

            String templateCode = stringBuilder.toString();

            header.setTotalAmount(BigDecimal.ZERO);
            header.setTaxAmount(BigDecimal.ZERO);
            header.setExcludeTaxAmount(BigDecimal.ZERO);

            header.setApplyHeaderNumber(templateCode);
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

        invoiceApplyLineService.saveData(lineWithHeaderIdList);
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
     * transform invoiceApplyHeader to appropriate DTO object.
     * Add the meaning of invoice type, color and apply status
     * @param invoiceApplyHeader invoice apply header object
     * @param organizationId tenant id
     * @return invoiceApplyHeaderDTO
     */
    private InvoiceApplyHeaderDTO mapToDto(InvoiceApplyHeader invoiceApplyHeader, Long organizationId) {

        InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();
        BeanUtils.copyProperties(invoiceApplyHeader, dto);

        // get value set for apply status
//        String applyStatus = lovAdapter.queryLovMeaning(
//                BaseConstant.InvApplyHeader.APPLY_STATUS_CODE,
//                organizationId,
//                invoiceApplyHeader.getApplyStatus(),
//                BaseConstant.LANGUAGE_CN
//        );
//
//        // get value set for invoice color
//        String invoiceColor = lovAdapter.queryLovMeaning(
//                BaseConstant.InvApplyHeader.INVOICE_COLOR_CODE,
//                organizationId,
//                invoiceApplyHeader.getInvoiceColor(),
//                BaseConstant.LANGUAGE_CN
//        );
//
//        // get value set for invoice type
//        String invoiceType = lovAdapter.queryLovMeaning(
//                InvApplyHeaderConstant.INVOICE_TYPE,
//                organizationId,
//                invoiceApplyHeader.getInvoiceType(),
//                BaseConstant.LANGUAGE_CN
//        );
//
//        dto.setApplyStatusMeaning(applyStatus);
//        dto.setInvoiceTypeMeaning(invoiceType);
//        dto.setInvoiceColorMeaning(invoiceColor);

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

