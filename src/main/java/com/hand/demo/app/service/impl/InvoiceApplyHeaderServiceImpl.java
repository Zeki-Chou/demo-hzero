package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.api.controller.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.IamUser;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.IamUserRepository;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.BaseConstant;
import com.hand.demo.infra.constant.InvApplyHeaderConstant;
import com.hand.demo.infra.mapper.InvoiceApplyHeaderMapper;
import com.hand.demo.infra.util.InvoiceApplyHeaderUtils;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
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

    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    private final InvoiceApplyLineRepository invoiceApplyLineRepository;

    private final InvoiceApplyLineService invoiceApplyLineService;

    private final IamRemoteService iamRemoteService;

    private static final Long REDIS_EXPIRE_DURATION = 60L;

    public InvoiceApplyHeaderServiceImpl(
            InvoiceApplyLineRepository invoiceApplyLineRepository,
            InvoiceApplyLineService invoiceApplyLineService,
            InvoiceApplyHeaderRepository invoiceApplyHeaderRepository,
            RedisHelper redis,
            InvoiceApplyHeaderMapper mapper,
            CodeRuleBuilder codeRuleBuilder,
            LovAdapter lovAdapter,
            IamRemoteService iamRemoteService
    ) {
        this.invoiceApplyLineRepository = invoiceApplyLineRepository;
        this.invoiceApplyLineService = invoiceApplyLineService;
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
        this.redis = redis;
        this.mapper = mapper;
        this.codeRuleBuilder = codeRuleBuilder;
        this.lovAdapter = lovAdapter;
        this.iamRemoteService = iamRemoteService;
    }

    @Override
    public Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeaderDTO invoiceApplyHeader, Long organizationId) {

        List<LovValueDTO> applyStatusLovValues = lovAdapter.queryLovValue(BaseConstant.InvApplyHeader.APPLY_STATUS_CODE, organizationId);
        List<LovValueDTO>  invoiceTypeLovValues = lovAdapter.queryLovValue(BaseConstant.InvApplyHeader.INVOICE_TYPE_CODE, organizationId);

        Map<String, String> applyStatusMap = applyStatusLovValues
                                                .stream()
                                                .collect(Collectors.toMap(LovValueDTO::getMeaning, LovValueDTO::getValue));
        Map<String, String> invoiceTypeMap = invoiceTypeLovValues
                                                .stream()
                                                .collect(Collectors.toMap(LovValueDTO::getMeaning, LovValueDTO::getValue));

        if (invoiceApplyHeader.getApplyStatusList() != null) {
            List<String> applyStatusMeaningList = invoiceApplyHeader
                                                    .getApplyStatusList()
                                                    .stream()
                                                    .map(applyStatusMap::get)
                                                    .collect(Collectors.toList());
            invoiceApplyHeader.setApplyStatusList(applyStatusMeaningList);
        }

        if (invoiceApplyHeader.getInvoiceType() != null && invoiceTypeMap.containsKey(invoiceApplyHeader.getInvoiceType())) {
            String invoiceTypeValue = invoiceTypeMap.get(invoiceApplyHeader.getInvoiceType());
            invoiceApplyHeader.setInvoiceType(invoiceTypeValue);
        }

        if (invoiceApplyHeader.getDelFlag() == null) {
            invoiceApplyHeader.setDelFlag(0);
        }

        String responseJsonString = getIamResponseBody();
        JSONObject iamJsonString = new JSONObject(responseJsonString);

        if (iamJsonString.has("tenantAdminFlag")) {
            Boolean isTenantAdmin = iamJsonString.getBoolean("tenantAdminFlag");
            invoiceApplyHeader.setTenantAdminFlag(isTenantAdmin);
        }

        if (iamJsonString.has("superTenantAdminFlag")) {
            Boolean isTenantAdmin = iamJsonString.getBoolean("superTenantAdminFlag");
            invoiceApplyHeader.setTenantAdminFlag(isTenantAdmin);
        }

        return PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyHeaderRepository.selectList(invoiceApplyHeader));
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

        if (Boolean.TRUE.equals(redis.hasKey(cacheName))) {
            String jsonString = redis.strGet(cacheName);
            return JSON.parseObject(jsonString, InvoiceApplyHeaderDTO.class);
        }

        InvoiceApplyHeaderDTO dtoRecord = new InvoiceApplyHeaderDTO();
        dtoRecord.setApplyHeaderId(applyHeaderId);
        InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectOne(dtoRecord);

        if (header == null) {
            throw new CommonException("demo-47359.warn.invoice_apply_header.not_found", applyHeaderId);
        }

        InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();
        BeanUtils.copyProperties(header, dto);
        InvoiceApplyLine invoiceApplyLine = new InvoiceApplyLine();
        invoiceApplyLine.setApplyHeaderId(applyHeaderId);
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineRepository.selectList(invoiceApplyLine);
        dto.setDataList(invoiceApplyLines);

        // get the name of the creator for the invoice header
        CustomUserDetails userDetail = DetailsHelper.getUserDetails();
        dto.setRealName(userDetail.getRealName());

        String jsonStringDto = JSON.toJSONString(dto);

        redis.strSet(cacheName, jsonStringDto);
        redis.setExpire(cacheName, REDIS_EXPIRE_DURATION);
        return dto;
    }

    @Override
    public List<InvoiceApplyHeaderDTO> exportAll(Long organizationId) {
        List<InvoiceApplyHeader> headers = invoiceApplyHeaderRepository.selectAll();
        List<InvoiceApplyHeaderDTO> headerDTOS = new ArrayList<>();

        for (InvoiceApplyHeader header : headers) {
            headerDTOS.add(mapToDto(header));
        }

        return headerDTOS;
    }

    /**
     * transform invoiceApplyHeader to appropriate DTO object.
     * Add the meaning of invoice type, color and apply status
     * @param invoiceApplyHeader invoice apply header object
     * @return invoiceApplyHeaderDTO
     */
    private InvoiceApplyHeaderDTO mapToDto(InvoiceApplyHeader invoiceApplyHeader) {
        InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();
        BeanUtils.copyProperties(invoiceApplyHeader, dto);
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

    private String getIamResponseBody() {
        ResponseEntity<String> iamResponse = iamRemoteService.selectSelf();

        if (!iamResponse.getStatusCode().equals(HttpStatus.OK)) {
            throw new CommonException("Error getting iam object");
        }

        return iamResponse.getBody();
    }
}

