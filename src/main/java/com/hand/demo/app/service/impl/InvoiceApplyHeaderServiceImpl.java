package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.hand.demo.api.dto.InvoiceApplyHeaderDto;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.InvoiceConstants;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.springframework.beans.BeanUtils;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author muhammad.azzam@hand-global.com
 * @since 2024-11-04 14:47:03
 */
@Service
public class InvoiceApplyHeaderServiceImpl implements InvoiceApplyHeaderService {

    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    private final InvoiceApplyLineRepository invoiceApplyLineRepository;
    private final CodeRuleBuilder codeRuleBuilder;
    private final LovAdapter lovAdapter;
    private final RedisHelper redisHelper;
    private final InvoiceApplyLineServiceImpl invoiceApplyLineService;

    public InvoiceApplyHeaderServiceImpl(InvoiceApplyHeaderRepository invoiceApplyHeaderRepository, InvoiceApplyLineRepository invoiceApplyLineRepository, CodeRuleBuilder codeRuleBuilder, LovAdapter lovAdapter, RedisHelper redisHelper, InvoiceApplyLineServiceImpl invoiceApplyLineService) {
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
        this.invoiceApplyLineRepository = invoiceApplyLineRepository;
        this.codeRuleBuilder = codeRuleBuilder;
        this.lovAdapter = lovAdapter;
        this.redisHelper = redisHelper;
        this.invoiceApplyLineService = invoiceApplyLineService;
    }

    @Override
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public Page<InvoiceApplyHeaderDto> selectList(PageRequest pageRequest, InvoiceApplyHeaderDto invoiceApplyHeaderDto) {
        // Get paginated InvoiceApplyHeader results
        Page<InvoiceApplyHeader> pageResult = PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyHeaderRepository.selectList(invoiceApplyHeaderDto));
        List<InvoiceApplyHeaderDto> invoiceApplyHeaderDTOS = new ArrayList<>();

        // Iterate InvoiceApplyHeader
        for (InvoiceApplyHeader data : pageResult) {

            // Map InvoiceApplyHeader to DTO
            InvoiceApplyHeaderDto dto = mapEntityToDto(data);

            // Add the mapped DTO to the list
            invoiceApplyHeaderDTOS.add(dto);
        }

        // DTOs Pagination
        Page<InvoiceApplyHeaderDto> dtoPage = new Page<>();
        dtoPage.setContent(invoiceApplyHeaderDTOS);
        dtoPage.setTotalPages(pageResult.getTotalPages());
        dtoPage.setTotalElements(pageResult.getTotalElements());
        dtoPage.setNumber(pageResult.getNumber());
        dtoPage.setSize(pageResult.getSize());

        return dtoPage;
    }

    @Override
    public List<InvoiceApplyHeaderDto> saveData(List<InvoiceApplyHeaderDto> invoiceApplyHeaderDtos) {
        validateInvoiceHeaders(invoiceApplyHeaderDtos);

        List<InvoiceApplyHeaderDto> responseList = new ArrayList<>();

        for (InvoiceApplyHeaderDto dto : invoiceApplyHeaderDtos) {
            handleSave(dto);
            responseList.add(dto);
        }

        return responseList;
    }

    private void validateInvoiceHeaders(List<InvoiceApplyHeaderDto> invoiceApplyHeaderDtos) {
        List<String> validApplyStatuses = lovAdapter.queryLovValue(InvoiceConstants.LovCode.APPLY_STATUS, BaseConstants.DEFAULT_TENANT_ID)
                .stream().map(LovValueDTO::getValue).collect(Collectors.toList());
        List<String> validInvoiceColors = lovAdapter.queryLovValue(InvoiceConstants.LovCode.INVOICE_COLOR, BaseConstants.DEFAULT_TENANT_ID)
                .stream().map(LovValueDTO::getValue).collect(Collectors.toList());
        List<String> validInvoiceTypes = lovAdapter.queryLovValue(InvoiceConstants.LovCode.INVOICE_TYPE, BaseConstants.DEFAULT_TENANT_ID)
                .stream().map(LovValueDTO::getValue).collect(Collectors.toList());

        List<String> invalidHeaders = new ArrayList<>();
        for (int i = 0; i < invoiceApplyHeaderDtos.size(); i++) {
            InvoiceApplyHeaderDto dto = invoiceApplyHeaderDtos.get(i);
            if (!validApplyStatuses.contains(dto.getApplyStatus())) {
                invalidHeaders.add(InvoiceConstants.LINE + (i + 1) + " - Invalid Apply Status: " + dto.getApplyStatus());
            }
            if (!validInvoiceColors.contains(dto.getInvoiceColor())) {
                invalidHeaders.add(InvoiceConstants.LINE + (i + 1) + " - Invalid Invoice Color: " + dto.getInvoiceColor());
            }
            if (!validInvoiceTypes.contains(dto.getInvoiceType())) {
                invalidHeaders.add(InvoiceConstants.LINE + (i + 1) + " - Invalid Invoice Type: " + dto.getInvoiceType());
            }
        }

        if (!invalidHeaders.isEmpty()) {
            throw new CommonException(InvoiceConstants.Exception.INVALID_APPLY_HEADER, String.join(", ", invalidHeaders));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    private void handleSave(InvoiceApplyHeaderDto dto) {

        InvoiceApplyHeader entity;

        if (dto.getApplyHeaderId() == null) {

            // Generate code rule
            Map<String, String> variableMap = new HashMap<>();
            variableMap.put("customSegment", "-");
            String uniqueCode = codeRuleBuilder.generateCode(InvoiceConstants.CODE_RULE, variableMap);
            dto.setApplyHeaderNumber(uniqueCode);

            entity = mapDtoToEntity(dto);
            invoiceApplyHeaderRepository.insertSelective(entity);
        } else {
            entity = mapDtoToEntity(dto);
            invoiceApplyHeaderRepository.updateByPrimaryKeySelective(entity);
        }

        // Process and save associated lines
        dto.getInvoiceLineList().forEach(line -> line.setApplyHeaderId(entity.getApplyHeaderId()));
        invoiceApplyLineService.saveData(dto.getInvoiceLineList());

        // Update totals in DTO based on the saved entity
        updateHeaderDtoTotals(dto, entity);
    }

    private void updateHeaderDtoTotals(InvoiceApplyHeaderDto dto, InvoiceApplyHeader entity) {
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setExcludeTaxAmount(entity.getExcludeTaxAmount());
        dto.setTaxAmount(entity.getTaxAmount());
    }

    @Override
    public void deleteInvoiceHeaders(List<InvoiceApplyHeader> invoiceApplyHeaders) {
        // Collect applyHeaderIds
        List<Long> applyHeaderIds = invoiceApplyHeaders.stream()
                .map(InvoiceApplyHeader::getApplyHeaderId)
                .collect(Collectors.toList());

        // Convert to comma-separated String
        String applyHeaderIdsStr = applyHeaderIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        // Query all headers by IDs in one go
        List<InvoiceApplyHeader> headersToUpdate = invoiceApplyHeaderRepository.selectByIds(applyHeaderIdsStr);

        // Delete cache keys for each ID
        applyHeaderIds.forEach(id -> redisHelper.delKey(InvoiceConstants.CACHE_KEY + id));

        // Check for any missing IDs
        List<Long> foundIds = headersToUpdate.stream()
                .map(InvoiceApplyHeader::getApplyHeaderId)
                .collect(Collectors.toList());

        List<Long> missingIds = applyHeaderIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());

        if (!missingIds.isEmpty()) {
            String msg = "ID(s) - " + missingIds.toString();
            throw new CommonException(InvoiceConstants.Exception.INVALID_APPLY_HEADER, msg);
        }

        // Set del_flag to 1 for each valid header
        headersToUpdate.forEach(header -> header.setDelFlag(1));

        // Perform batch update
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(headersToUpdate);
    }

    @Override
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public InvoiceApplyHeaderDto getInvoiceDetailById(Long applyHeaderId) {
        String cacheKey = InvoiceConstants.CACHE_KEY + applyHeaderId;

        // Check data in Redis cache
        if (Boolean.TRUE.equals(redisHelper.hasKey(cacheKey))) {
            String serializedHeader = redisHelper.strGet(cacheKey);
            return deserialize(serializedHeader);
        }

        // Not cached then query data
        InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectByPrimaryKey(applyHeaderId);
        if (header == null) {
            String msg = "ID(s) - " + applyHeaderId;
            throw new CommonException(InvoiceConstants.Exception.INVALID_APPLY_HEADER, msg);
        }

        InvoiceApplyHeaderDto dto = mapEntityToDto(header);

        // query lines and set in DTO
        List<InvoiceApplyLine> lineItems = invoiceApplyLineRepository.select(InvoiceApplyHeader.FIELD_APPLY_HEADER_ID, applyHeaderId);
        dto.setInvoiceLineList(lineItems);

        // cache data
        String serializedDto = serialize(dto);
        redisHelper.strSet(cacheKey, serializedDto);

        return dto;
    }

    //Maps DTO to InvoiceApplyHeader
    private InvoiceApplyHeader mapDtoToEntity(InvoiceApplyHeaderDto dto) {
        InvoiceApplyHeader entity = new InvoiceApplyHeader();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    //Maps InvoiceApplyHeader to DTO
    private InvoiceApplyHeaderDto mapEntityToDto(InvoiceApplyHeader entity) {
        InvoiceApplyHeaderDto dto = new InvoiceApplyHeaderDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    //JSON to String
    private String serialize(InvoiceApplyHeaderDto invoiceApplyHeaderDto) {
        try {
            return JSON.toJSONString(invoiceApplyHeaderDto);
        } catch (JSONException e) {
            throw new CommonException("Error serializing object", e);
        }
    }

    //Parse Object
    private InvoiceApplyHeaderDto deserialize(String data) {
        try {
            return JSON.parseObject(data, InvoiceApplyHeaderDto.class);
        } catch (JSONException e) {
            throw new CommonException("Error deserializing object", e);
        }
    }
}
