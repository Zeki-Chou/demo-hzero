package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.api.dto.IamDTO;
import com.hand.demo.api.dto.InvCountHeaderDTO;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.dto.OrderHeaderDTO;
import com.hand.demo.domain.entity.*;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.InvHeaderConstant;
import com.hand.demo.infra.constant.TaskConstant;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.hzero.mybatis.domian.Condition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author Fatih Khoiri
 * @since 2024-11-04 10:14:16
 */
@Service
public class InvoiceApplyHeaderServiceImpl implements InvoiceApplyHeaderService {
    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private LovAdapter lovAdapter;

    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    private CodeRuleBuilder codeRuleBuilder;

    @Autowired
    private RedisHelper redisHelper;

    @Override
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader) {
        Page<InvoiceApplyHeader> pageResult = PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyHeaderRepository.selectList(invoiceApplyHeader));
        List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOS = new ArrayList<>();
        for (InvoiceApplyHeader data : pageResult) {
            invoiceApplyHeaderDTOS.add(changeToDTO(data));
        }

        for (InvoiceApplyHeaderDTO dataDTO: invoiceApplyHeaderDTOS) {
            dataDTO.setInvoiceApplyLines(invoiceApplyLineRepository.select(InvoiceApplyLine.FIELD_APPLY_HEADER_ID,
                    dataDTO.getApplyHeaderId()));
        }

        Page<InvoiceApplyHeaderDTO> dtoPage = new Page<>();
        dtoPage.setContent(invoiceApplyHeaderDTOS);
        dtoPage.setTotalPages(pageResult.getTotalPages());
        dtoPage.setTotalElements(pageResult.getTotalElements());
        dtoPage.setNumber(pageResult.getNumber());
        dtoPage.setSize(pageResult.getSize());

        return dtoPage;
    }

//    @Override
//    public void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders) {
////        validaetionOfHeader(invoiceApplyHeaders);
//
//        List<InvoiceApplyHeader> insertList = invoiceApplyHeaders.stream().filter(
//                line -> line.getApplyHeaderId() == null)
//                .collect(Collectors.toList());
//
//        invoiceApplyHeaderRepository.batchInsertSelective(insertList);
//
//
//        List<InvoiceApplyHeader> updateList = invoiceApplyHeaders.stream().filter(
//                        line -> line.getApplyHeaderId() != null)
//                .collect(Collectors.toList());
//        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateList);
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders) {
        validationOfHeader(invoiceApplyHeaders);

        List<InvoiceApplyHeader> insertList = filterInsertList(invoiceApplyHeaders);
        List<InvoiceApplyHeader> updateList = filterUpdateList(invoiceApplyHeaders);

        if (!insertList.isEmpty()) {
            processInsertList(insertList);
        }

        if (!updateList.isEmpty()) {
            processUpdateList(updateList);
        }
    }

    private List<InvoiceApplyHeader> filterInsertList(List<InvoiceApplyHeaderDTO> headers) {
        return headers.stream()
                .filter(header -> header.getApplyHeaderId() == null)
                .collect(Collectors.toList());
    }

    private List<InvoiceApplyHeader> filterUpdateList(List<InvoiceApplyHeaderDTO> headers) {
        return headers.stream()
                .filter(header -> header.getApplyHeaderId() != null)
                .collect(Collectors.toList());
    }

    private void processInsertList(List<InvoiceApplyHeader> insertList) {
        Map<String, String> variableMap = initializeVariableMap();

        for (InvoiceApplyHeader header : insertList) {
            initializeHeaderAmounts(header);
            invoiceApplyHeaderRepository.insert(header);

            Long generatedId = header.getApplyHeaderId();
            InvoiceApplyHeaderDTO headerDTO = changeToDTO(header);

            if (headerDTO.getInvoiceApplyLines() != null && !headerDTO.getInvoiceApplyLines().isEmpty()) {
                processInvoiceApplyLines(headerDTO.getInvoiceApplyLines(), generatedId);
                updateHeaderAmounts(header, headerDTO.getInvoiceApplyLines(), variableMap);
            }
        }
    }

    private Map<String, String> initializeVariableMap() {
        Map<String, String> variableMap = new HashMap<>();
        variableMap.put("customSegment", "-");
        return variableMap;
    }

    private void initializeHeaderAmounts(InvoiceApplyHeader header) {
        header.setTotalAmount(BigDecimal.valueOf(0));
        header.setExcludeTaxAmount(BigDecimal.valueOf(0));
        header.setTaxAmount(BigDecimal.valueOf(0));
    }

    private void processInvoiceApplyLines(List<InvoiceApplyLine> lines, Long headerId) {
        for (InvoiceApplyLine line : lines) {
            line.setApplyHeaderId(headerId);
            calculateLineAmounts(line);
        }

        List<InvoiceApplyLine> newLines = lines.stream()
                .filter(line -> line.getApplyLineId() == null)
                .collect(Collectors.toList());

        if (!newLines.isEmpty()) {
            invoiceApplyLineRepository.batchInsert(newLines);
        }
    }

    private void calculateLineAmounts(InvoiceApplyLine line) {
        line.setTotalAmount(line.getUnitPrice().multiply(line.getQuantity()));
        line.setTaxAmount(line.getTotalAmount().multiply(line.getTaxRate()));
        line.setExcludeTaxAmount(line.getTotalAmount().subtract(line.getTaxAmount()));
    }

    private void updateHeaderAmounts(InvoiceApplyHeader header, List<InvoiceApplyLine> lines, Map<String, String> variableMap) {
        BigDecimal totalAmount = calculateTotalAmount(lines, InvoiceApplyLine::getTotalAmount);
        BigDecimal excludeTaxAmount = calculateTotalAmount(lines, InvoiceApplyLine::getExcludeTaxAmount);
        BigDecimal taxAmount = calculateTotalAmount(lines, InvoiceApplyLine::getTaxAmount);

        header.setTotalAmount(totalAmount);
        header.setExcludeTaxAmount(excludeTaxAmount);
        header.setTaxAmount(taxAmount);
        header.setApplyHeaderNumber(codeRuleBuilder.generateCode(InvHeaderConstant.RULE_CODE, variableMap));

        redisHelper.delKey(header.getApplyHeaderNumber());
        invoiceApplyHeaderRepository.updateByPrimaryKey(header);
    }

    private BigDecimal calculateTotalAmount(List<InvoiceApplyLine> lines, Function<InvoiceApplyLine, BigDecimal> mapper) {
        return lines.stream()
                .map(mapper)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void processUpdateList(List<InvoiceApplyHeader> updateList) {
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateList);

        for (InvoiceApplyHeader header : updateList) {
            InvoiceApplyHeaderDTO headerDTO = changeToDTO(header);

            if (headerDTO.getInvoiceApplyLines() != null && !headerDTO.getInvoiceApplyLines().isEmpty()) {
                processExistingAndNewLines(headerDTO.getInvoiceApplyLines(), header.getApplyHeaderId());
                updateHeaderAmountsForExisting(header, headerDTO.getInvoiceApplyLines());
            }
        }
    }

    private void processExistingAndNewLines(List<InvoiceApplyLine> lines, Long headerId) {
        List<InvoiceApplyLine> newLines = lines.stream()
                .filter(line -> line.getApplyLineId() == null)
                .collect(Collectors.toList());

        List<InvoiceApplyLine> existingLines = lines.stream()
                .filter(line -> line.getApplyLineId() != null)
                .collect(Collectors.toList());

        for (InvoiceApplyLine line : newLines) {
            line.setApplyHeaderId(headerId);
            calculateLineAmounts(line);
            invoiceApplyLineRepository.insert(line);
        }

        for (InvoiceApplyLine line : existingLines) {
            calculateLineAmounts(line);
            invoiceApplyLineRepository.updateByPrimaryKey(line);
        }
    }

    private void updateHeaderAmountsForExisting(InvoiceApplyHeader header, List<InvoiceApplyLine> lines) {
        BigDecimal totalAmount = calculateTotalAmount(lines, InvoiceApplyLine::getTotalAmount);
        BigDecimal excludeTaxAmount = calculateTotalAmount(lines, InvoiceApplyLine::getExcludeTaxAmount);
        BigDecimal taxAmount = calculateTotalAmount(lines, InvoiceApplyLine::getTaxAmount);

        header.setTotalAmount(totalAmount);
        header.setExcludeTaxAmount(excludeTaxAmount);
        header.setTaxAmount(taxAmount);

        redisHelper.delKey(header.getApplyHeaderNumber());
        invoiceApplyHeaderRepository.updateByPrimaryKey(header);
    }



    @Override
    public InvoiceApplyHeaderDTO delete(Long id) {
        InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();
        InvoiceApplyHeader data = invoiceApplyHeaderRepository.selectByPrimary(id);
        if (data != null) {
            data.setDelFlag(1);
            dto = changeToDTO(data);
        } else {
            throw new CommonException("Data hasnt found");
        }
        redisHelper.delKey(dto.getApplyHeaderNumber());
        return dto;
    }

    @Override
    public InvoiceApplyHeaderDTO detail(Long id) {
        InvoiceApplyHeader getHeader = invoiceApplyHeaderRepository.selectByPrimaryKey(id);
        List<InvoiceApplyLine> listLines= invoiceApplyLineRepository.select("applyHeaderId", getHeader.getApplyHeaderId());
        InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();
        BeanUtils.copyProperties(getHeader, dto);
        dto.setInvoiceApplyLines(listLines);

        if (redisHelper.hasKey(dto.getApplyHeaderNumber())) {
            String result = redisHelper.strGet(dto.getApplyHeaderNumber());
            if (result != null || !result.isEmpty()) {
                return JSON.parseObject(result, InvoiceApplyHeaderDTO.class);
            }
        }

        String serializeDTO = JSON.toJSONString(dto);
        redisHelper.strSet(dto.getApplyHeaderNumber(), serializeDTO);

        return dto;
    }


    private void validationOfHeader(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders) {
        List<LovValueDTO> validApplyTypesList = lovAdapter.queryLovValue(InvHeaderConstant.APPLY_TYPE_CODE,
                Long.valueOf(TaskConstant.TENANT_ID));
        List<LovValueDTO> validColorTypesList = lovAdapter.queryLovValue(InvHeaderConstant.INVOICE_COLOR_CODE,
                Long.valueOf(TaskConstant.TENANT_ID));
        List<LovValueDTO> validStatusTypesList = lovAdapter.queryLovValue(InvHeaderConstant.APPLY_STATUS_CODE,
                Long.valueOf(TaskConstant.TENANT_ID));

        List<String> validColorTypes = validColorTypesList.stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        List<String> validApplyTypes = validApplyTypesList.stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        List<String> validStatusTypes = validStatusTypesList.stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        StringBuilder errorMessages = new StringBuilder();
        for (int i = 0; i < invoiceApplyHeaders.size(); i++) {
            InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaders.get(i);
            StringBuilder lineError = new StringBuilder();

            if (!validColorTypes.contains(invoiceApplyHeader.getInvoiceColor())) {
                lineError.append("Invoice color: ").append(invoiceApplyHeader.getInvoiceColor()).append(", ");
            }
            if (!validApplyTypes.contains(invoiceApplyHeader.getInvoiceType())) {
                lineError.append("Invoice Type: ").append(invoiceApplyHeader.getInvoiceType()).append(", ");
            }
            if (!validStatusTypes.contains(invoiceApplyHeader.getApplyStatus())) {
                lineError.append("Apply status: ").append(invoiceApplyHeader.getApplyStatus()).append(", ");
            }

            if (lineError.length() > 0) {
                errorMessages.append("line num: ").append(i + 1).append(", ").append(lineError).append("\n");
            }
        }

        String finalErrorMessages = errorMessages.toString();
        if (errorMessages.length() > 0) {
            throw new CommonException(finalErrorMessages);
        }
    }

    private InvoiceApplyHeaderDTO changeToDTO(InvoiceApplyHeader invoiceApplyHeaderDTO) {
        InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();
        BeanUtils.copyProperties(invoiceApplyHeaderDTO, dto);
        return dto;
    }
}

