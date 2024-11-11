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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public InvoiceApplyHeaderServiceImpl(InvoiceApplyHeaderRepository invoiceApplyHeaderRepository, InvoiceApplyLineRepository invoiceApplyLineRepository, CodeRuleBuilder codeRuleBuilder, LovAdapter lovAdapter, RedisHelper redisHelper) {
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
        this.invoiceApplyLineRepository = invoiceApplyLineRepository;
        this.codeRuleBuilder = codeRuleBuilder;
        this.lovAdapter = lovAdapter;
        this.redisHelper = redisHelper;
    }

    @Override
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public Page<InvoiceApplyHeaderDto> selectList(PageRequest pageRequest, InvoiceApplyHeaderDto invoiceApplyHeaderDto) {
        // Get paginated InvoiceApplyHeader results
        Page<InvoiceApplyHeader> pageResult = PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyHeaderRepository.selectList(invoiceApplyHeaderDto));
        List<InvoiceApplyHeaderDto> invoiceApplyHeaderDTOS = new ArrayList<>();

        // Iterate through each InvoiceApplyHeader and map it to the DTO
        for (InvoiceApplyHeader data : pageResult) {

            // Map InvoiceApplyHeader to DTO
            InvoiceApplyHeaderDto dto = mapEntityToDto(data);

            // Add the mapped DTO to the list
            invoiceApplyHeaderDTOS.add(dto);
        }

        // Create a Page for the DTOs with pagination information
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

        // Fetch valid values from LOV for validations
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
            throw new CommonException("Invalid invoice apply headers found: " + String.join(", ", invalidHeaders));
        }

        List<InvoiceApplyHeaderDto> responseList = new ArrayList<>();

        // Separate the insert list and update list
        List<InvoiceApplyHeaderDto> insertList = invoiceApplyHeaderDtos.stream()
                .filter(dto -> dto.getApplyHeaderId() == null)
                .collect(Collectors.toList());
        List<InvoiceApplyHeaderDto> updateList = invoiceApplyHeaderDtos.stream()
                .filter(dto -> dto.getApplyHeaderId() != null)
                .collect(Collectors.toList());

        // Process inserts
        insertList.forEach(dto -> {
            Map<String, String> variableMap = new HashMap<>();
            variableMap.put("customSegment", "-");
            String uniqueCode = codeRuleBuilder.generateCode(InvoiceConstants.CODE_RULE, variableMap);
            dto.setApplyHeaderNumber(uniqueCode);

            InvoiceApplyHeader entity = mapDtoToEntity(dto);

            // Calculate totals and insert
            invoiceApplyHeaderRepository.insertSelective(entity);
            processInvoiceLines(dto.getInvoiceLineList(), entity);

            // Update DTO with calculated totals from the entity
            dto.setApplyHeaderId(entity.getApplyHeaderId());
            dto.setTotalAmount(entity.getTotalAmount());
            dto.setExcludeTaxAmount(entity.getExcludeTaxAmount());
            dto.setTaxAmount(entity.getTaxAmount());

            redisHelper.delKey(InvoiceConstants.CACHE_KEY + entity.getApplyHeaderId());

            responseList.add(dto);
        });

        // Process updates
        updateList.forEach(dto -> {
            InvoiceApplyHeader entity = mapDtoToEntity(dto);

            // Process lines and calculate totals
            processInvoiceLines(dto.getInvoiceLineList(), entity);

            // Update the existing header with new totals
            invoiceApplyHeaderRepository.updateByPrimaryKeySelective(entity);

            // Update DTO with calculated totals from the entity
            dto.setTotalAmount(entity.getTotalAmount());
            dto.setExcludeTaxAmount(entity.getExcludeTaxAmount());
            dto.setTaxAmount(entity.getTaxAmount());

            responseList.add(dto); // `dto` now includes updated totals and `invoiceLineList`
        });

        return responseList;
    }


    //Process the invoice lines for header
    private void processInvoiceLines(List<InvoiceApplyLine> invoiceLines, InvoiceApplyHeader header) {
        BigDecimal headerTotalAmount = BigDecimal.ZERO;
        BigDecimal headerExcludeTaxAmount = BigDecimal.ZERO;
        BigDecimal headerTaxAmount = BigDecimal.ZERO;

        if (invoiceLines != null && !invoiceLines.isEmpty()) {
            for (InvoiceApplyLine line : invoiceLines) {
                // Set the applyHeaderId from the parent header
                line.setApplyHeaderId(header.getApplyHeaderId());

                // Calculate line-level fields
                BigDecimal lineTotalAmount = line.getUnitPrice().multiply(line.getQuantity());
                line.setTotalAmount(lineTotalAmount);

                BigDecimal lineTaxAmount = lineTotalAmount.multiply(line.getTaxRate());
                line.setTaxAmount(lineTaxAmount);

                BigDecimal lineExcludeTaxAmount = lineTotalAmount.subtract(lineTaxAmount);
                line.setExcludeTaxAmount(lineExcludeTaxAmount);

                // Accumulate to header-level totals
                headerTotalAmount = headerTotalAmount.add(lineTotalAmount);
                headerExcludeTaxAmount = headerExcludeTaxAmount.add(lineExcludeTaxAmount);
                headerTaxAmount = headerTaxAmount.add(lineTaxAmount);
            }

            // Save lines to the database
            invoiceApplyLineRepository.batchInsert(invoiceLines);
        }

        // Set header-level totals after processing all lines
        header.setTotalAmount(headerTotalAmount);
        header.setExcludeTaxAmount(headerExcludeTaxAmount);
        header.setTaxAmount(headerTaxAmount);
    }

    @Override
    public void deleteInvoiceHeaders(List<InvoiceApplyHeader> invoiceApplyHeaders) {
        for (InvoiceApplyHeader header : invoiceApplyHeaders) {
            // Fetch the invoice header entity by ID
            InvoiceApplyHeader entity = invoiceApplyHeaderRepository.selectByPrimaryKey(header.getApplyHeaderId());

            redisHelper.delKey(InvoiceConstants.CACHE_KEY + header.getApplyHeaderId());
            if (entity != null) {
                // Set the del_flag to 1 to mark it as deleted
                entity.setDelFlag(1);
                // Update the invoice header in the database
                invoiceApplyHeaderRepository.updateByPrimaryKeySelective(entity);
            } else {
                throw new CommonException("Invoice header not found with ID: " + header.getApplyHeaderId());
            }
        }
    }

    @Override
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public InvoiceApplyHeaderDto getInvoiceDetailById(Long applyHeaderId) {
        String cacheKey = InvoiceConstants.CACHE_KEY + applyHeaderId;

        // Check if data is available in Redis cache
        if (Boolean.TRUE.equals(redisHelper.hasKey(cacheKey))) {
            String serializedHeader = redisHelper.strGet(cacheKey);
            return deserialize(serializedHeader);
        }

        // Fetch the header by ID if not cached
        InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectByPrimaryKey(applyHeaderId);
        if (header == null) {
            throw new CommonException("InvoiceApplyHeader not found for ID: " + applyHeaderId);
        }

        // Map the header entity to DTO
        InvoiceApplyHeaderDto dto = mapEntityToDto(header);

        // Fetch associated line items and set in DTO
        List<InvoiceApplyLine> lineItems = invoiceApplyLineRepository.select(InvoiceApplyHeader.FIELD_APPLY_HEADER_ID, applyHeaderId);
        dto.setInvoiceLineList(lineItems);

        // Serialize and cache the DTO for future requests
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
