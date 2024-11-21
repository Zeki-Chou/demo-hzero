package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.api.controller.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.controller.dto.InvoiceApplyInfoDTO;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.BaseConstant;
import com.hand.demo.infra.constant.InvApplyHeaderConstant;
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
    private final RedisHelper redis;
    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    private final InvoiceApplyLineRepository invoiceApplyLineRepository;
    private final InvoiceApplyLineService invoiceApplyLineService;
    private final IamRemoteService iamRemoteService;

    public InvoiceApplyHeaderServiceImpl(
            InvoiceApplyLineRepository invoiceApplyLineRepository,
            InvoiceApplyLineService invoiceApplyLineService,
            InvoiceApplyHeaderRepository invoiceApplyHeaderRepository,
            RedisHelper redis,
            CodeRuleBuilder codeRuleBuilder,
            LovAdapter lovAdapter,
            IamRemoteService iamRemoteService
    ) {
        this.invoiceApplyLineRepository = invoiceApplyLineRepository;
        this.invoiceApplyLineService = invoiceApplyLineService;
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
        this.redis = redis;
        this.codeRuleBuilder = codeRuleBuilder;
        this.lovAdapter = lovAdapter;
        this.iamRemoteService = iamRemoteService;
    }

    @Override
    public Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeaderDTO invoiceApplyHeader, Long organizationId) {
        return PageHelper.doPageAndSort(pageRequest, () -> getInvoiceApplyHeaderDTOList(invoiceApplyHeader,  organizationId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders, Long organizationId) {
        // validate
        validateHeader(invoiceApplyHeaders, organizationId);
        Map<String, List<InvoiceApplyLine>> lineListMap = new HashMap<>();
        List<InvoiceApplyLine> lineWithHeaderIdList = new ArrayList<>();

        // put list of invoice lines into map
        for (InvoiceApplyHeaderDTO invoiceApplyHeader : invoiceApplyHeaders) {
            StringBuilder stringBuilder = new StringBuilder();

            if (invoiceApplyHeader.getApplyHeaderNumber() == null) {
                String newTemplateCode = generateInvoiceCode();
                stringBuilder.append(newTemplateCode);
            } else {
                stringBuilder.append(invoiceApplyHeader.getApplyHeaderNumber());
            }

            String templateCode = stringBuilder.toString();

            invoiceApplyHeader.setTotalAmount(BigDecimal.ZERO);
            invoiceApplyHeader.setTaxAmount(BigDecimal.ZERO);
            invoiceApplyHeader.setExcludeTaxAmount(BigDecimal.ZERO);

            invoiceApplyHeader.setApplyHeaderNumber(templateCode);
            lineListMap.put(templateCode, invoiceApplyHeader.getInvoiceApplyLines());
        }

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
            String key = header.getApplyHeaderId() + BaseConstant.InvApplyHeader.CACHE_PREFIX;
            deleteCache(key);
        }

        invoiceApplyLineService.saveData(lineWithHeaderIdList);
    }

    @Override
    public void deleteData(InvoiceApplyHeader invoiceApplyHeader) {
        if (invoiceApplyHeaderRepository.selectByPrimary(invoiceApplyHeader.getApplyHeaderId()) == null) {
            throw new CommonException("demo-47359.warn.invoice_apply_line.not_found", invoiceApplyHeader.getApplyHeaderId());
        }
        invoiceApplyHeaderRepository.deleteWithFlag(invoiceApplyHeader);
    }

    @Override
    public InvoiceApplyHeaderDTO detail(Long applyHeaderId) {

        // attempt to get cached invoice header dto from redis
        String key = applyHeaderId + BaseConstant.InvApplyHeader.CACHE_PREFIX;
        String cacheDtoString = getCacheData(key);
        if (!cacheDtoString.isEmpty()) {
            return JSON.parseObject(cacheDtoString, InvoiceApplyHeaderDTO.class);
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
        dto.setInvoiceApplyLines(invoiceApplyLines);

        // get the name of the creator for the invoice header
        CustomUserDetails userDetail = DetailsHelper.getUserDetails();
        dto.setRealName(userDetail.getRealName());
        cacheData(dto, dto.getApplyHeaderId() + BaseConstant.InvApplyHeader.CACHE_PREFIX);

        return dto;
    }

    @Override
    public List<InvoiceApplyHeaderDTO> exportAll(Long organizationId) {
        List<InvoiceApplyHeader> headers = invoiceApplyHeaderRepository.selectAll();
        return headers.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public InvoiceApplyInfoDTO getApplyInfo(InvoiceApplyInfoDTO invoiceApplyInfoDTO, Long organizationId) {
        InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();
        BeanUtils.copyProperties(invoiceApplyInfoDTO, dto);

        List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOS = getInvoiceApplyHeaderDTOList(dto, organizationId);
        List<InvoiceApplyLine> invoiceApplyLines = getInvoiceApplyLines(invoiceApplyHeaderDTOS);

        if (!invoiceApplyLines.isEmpty()) {
            Map<Long, String> listMap = invoiceApplyLines
                    .stream()
                    .collect(Collectors.groupingBy(
                            InvoiceApplyLine::getApplyHeaderId, // Group by applyHeaderId
                            Collectors.mapping(
                                    InvoiceApplyLine::getInvoiceName, // Map to invoiceName
                                    Collectors.joining(",") // Join names with a comma
                            )
                    ));
            invoiceApplyHeaderDTOS.forEach(header->header.setInvoiceNames(listMap.get(header.getApplyHeaderId())));
        }

        JSONObject iamJsonString = getIamJSONObject();
        invoiceApplyInfoDTO.setTenantName(iamJsonString.getString(BaseConstant.Iam.IAM_TENANTNAME));

        invoiceApplyInfoDTO.setInvoiceApplyHeaderDTOS(invoiceApplyHeaderDTOS);
        return invoiceApplyInfoDTO;
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
        List<String> applyStatusList = getLovValueList(BaseConstant.InvApplyHeader.APPLY_STATUS_CODE, organizationId);
        List<String> invoiceColorList = getLovValueList(BaseConstant.InvApplyHeader.INVOICE_COLOR_CODE, organizationId);
        List<String> invoiceTypeList =  getLovValueList(BaseConstant.InvApplyHeader.INVOICE_TYPE_CODE, organizationId);

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

    /**
     * make get request to get response entity for Iam response body.
     * throw error if response status not return 200
     * @return iam response body
     */
    private JSONObject getIamJSONObject() {
        ResponseEntity<String> iamResponse = iamRemoteService.selectSelf();

        if (!iamResponse.getStatusCode().equals(HttpStatus.OK)) {
            throw new CommonException("Error getting iam object");
        }

        String responseBody = iamResponse.getBody();
        return new JSONObject(responseBody);
    }

    private List<InvoiceApplyHeaderDTO> getInvoiceApplyHeaderDTOList(InvoiceApplyHeaderDTO invoiceApplyHeader, Long organizationId) {
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

        JSONObject iamJsonString = getIamJSONObject();

        if (iamJsonString.has("tenantAdminFlag")) {
            Boolean isTenantAdmin = iamJsonString.getBoolean("tenantAdminFlag");
            invoiceApplyHeader.setTenantAdminFlag(isTenantAdmin);
        }

        if (iamJsonString.has("superTenantAdminFlag")) {
            Boolean isTenantAdmin = iamJsonString.getBoolean("superTenantAdminFlag");
            invoiceApplyHeader.setTenantAdminFlag(isTenantAdmin);
        }

        return invoiceApplyHeaderRepository.selectList(invoiceApplyHeader);
    }

    /**
     * set invoice apply lines to corresponding invoice apply header in the
     * dataList attribute
     * @param invoiceApplyHeaderList  list of invoice apply header
     */
    private List<InvoiceApplyLine> getInvoiceApplyLines(List<InvoiceApplyHeaderDTO> invoiceApplyHeaderList) {
        if (invoiceApplyHeaderList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> invoiceApplyHeaderIds = invoiceApplyHeaderList
                .stream()
                .map(InvoiceApplyHeader::getApplyHeaderId)
                .collect(Collectors.toList());

        return invoiceApplyLineRepository.selectByHeaderIds(invoiceApplyHeaderIds);
    }

    /**
     * get cached JSON string
     * @param key key
     * @return JSON string
     */
    private String getCacheData(String key) {
        redis.setCurrentDatabase(13);

        if (Boolean.TRUE.equals(redis.hasKey(key))) {
            return redis.strGet(key);
        }

        return "";
    }

    /**
     * delete value from cache with associated key
     * @param key key
     */
    private void deleteCache(String key) {
        redis.setCurrentDatabase(13);
        redis.delKey(key);
    }

    /**
     * cache invoice apply header dto
     * @param dto invoice apply header dto
     * @param key key to store the dto into cache
     */
    private void cacheData(InvoiceApplyHeaderDTO dto, String key) {
        redis.setCurrentDatabase(13);
        String jsonStringDto = JSON.toJSONString(dto);
        redis.strSet(key, jsonStringDto);
        redis.setExpire(key, BaseConstant.Redis.EXPIRE_DURATION);
    }

    /**
     * generate invoice header number
     * @return invoice header number with format
     */
    private String generateInvoiceCode() {
        Map<String, String> variableMap = new HashMap<>();
        variableMap.put("customSegment", "-");
        return codeRuleBuilder.generateCode(InvApplyHeaderConstant.RULE_CODE, variableMap);
    }

    private List<String> getLovValueList(String lovCode, Long organizationId) {
        return lovAdapter
                    .queryLovValue(lovCode, organizationId)
                    .stream()
                    .map(LovValueDTO::getValue)
                    .collect(Collectors.toList());
    }
}

