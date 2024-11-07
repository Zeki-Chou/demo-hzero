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
        List<InvoiceApplyHeader> insertList = invoiceApplyHeaders.stream()
                .filter(header -> header.getApplyHeaderId() == null)
                .collect(Collectors.toList());

        List<InvoiceApplyHeader> updateList = invoiceApplyHeaders.stream()
                .filter(header -> header.getApplyHeaderId() != null)
                .collect(Collectors.toList());

        if (!insertList.isEmpty()) {
            Map<String, String> variableMap = new HashMap<>();
            variableMap.put("customSegment", "-");

            for (InvoiceApplyHeader header : insertList) {
                header.setTotalAmount(BigDecimal.valueOf(0));
                header.setExcludeTaxAmount(BigDecimal.valueOf(0));
                header.setTaxAmount(BigDecimal.valueOf(0));
                invoiceApplyHeaderRepository.insert(header);
                Long generatedId = header.getApplyHeaderId();

                InvoiceApplyHeaderDTO headerDTO = changeToDTO(header);

                if (headerDTO.getInvoiceApplyLines() != null && !headerDTO.getInvoiceApplyLines().isEmpty()) {
                    headerDTO.getInvoiceApplyLines().forEach(line -> {
                        line.setApplyHeaderId(generatedId);
                        line.setTotalAmount(line.getUnitPrice().multiply(line.getQuantity()));
                        line.setTaxAmount(line.getTotalAmount().multiply(line.getTaxRate()));
                        line.setExcludeTaxAmount(line.getTotalAmount().subtract(line.getTaxAmount()));
                    });

                    List<InvoiceApplyLine> newLines = headerDTO.getInvoiceApplyLines().stream()
                            .filter(line -> line.getApplyLineId() == null)
                            .collect(Collectors.toList());

                    if (!newLines.isEmpty()) {
                        invoiceApplyLineRepository.batchInsert(newLines);
                    }

                    BigDecimal totalAmount = newLines.stream()
                            .map(InvoiceApplyLine::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal excludeTaxAmount = newLines.stream()
                            .map(InvoiceApplyLine::getExcludeTaxAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal taxAmount = newLines.stream()
                            .map(InvoiceApplyLine::getTaxAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    String batchCodes = codeRuleBuilder.generateCode(InvHeaderConstant.RULE_CODE, variableMap);
                    header.setTotalAmount(totalAmount);
                    header.setExcludeTaxAmount(excludeTaxAmount);
                    header.setTaxAmount(taxAmount);
                    header.setApplyHeaderNumber(batchCodes);
                    redisHelper.delKey(header.getApplyHeaderNumber());
                    invoiceApplyHeaderRepository.updateByPrimaryKey(header);
                }
            }
        }


        if (!updateList.isEmpty()) {
            invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateList);

            for (InvoiceApplyHeader header : updateList) {
                InvoiceApplyHeaderDTO headerDTO = changeToDTO(header);

                if (headerDTO.getInvoiceApplyLines() != null && !headerDTO.getInvoiceApplyLines().isEmpty()) {
                    List<InvoiceApplyLine> newLines = headerDTO.getInvoiceApplyLines().stream()
                            .filter(line -> line.getApplyLineId() == null)
                            .collect(Collectors.toList());

                    List<InvoiceApplyLine> existingLines = headerDTO.getInvoiceApplyLines().stream()
                            .filter(line -> line.getApplyLineId() != null)
                            .collect(Collectors.toList());

                    for(InvoiceApplyLine line : newLines) {
                        line.setApplyHeaderId(header.getApplyHeaderId());
                        line.setTotalAmount(line.getUnitPrice().multiply(line.getQuantity()));
                        line.setTaxAmount(line.getTotalAmount().multiply(line.getTaxRate()));
                        line.setExcludeTaxAmount(line.getTotalAmount().subtract(line.getTaxAmount()));

                        invoiceApplyLineRepository.insert(line);
                    }


                    for (InvoiceApplyLine line : existingLines) {
                        line.setTotalAmount(line.getUnitPrice().multiply(line.getQuantity()));
                        line.setTaxAmount(line.getTotalAmount().multiply(line.getTaxRate()));
                        line.setExcludeTaxAmount(line.getTotalAmount().subtract(line.getTaxAmount()));

                        invoiceApplyLineRepository.updateByPrimaryKey(line);
                    }

                    BigDecimal totalAmount = headerDTO.getInvoiceApplyLines().stream()
                            .map(InvoiceApplyLine::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal excludeTaxAmount = headerDTO.getInvoiceApplyLines().stream()
                            .map(InvoiceApplyLine::getExcludeTaxAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal taxAmount = headerDTO.getInvoiceApplyLines().stream()
                            .map(InvoiceApplyLine::getTaxAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    header.setTotalAmount(totalAmount);
                    header.setExcludeTaxAmount(excludeTaxAmount);
                    header.setTaxAmount(taxAmount);
                    redisHelper.delKey(header.getApplyHeaderNumber());
                    invoiceApplyHeaderRepository.updateByPrimaryKey(header);
                }
            }
        }

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
        Condition condition = new Condition(InvoiceApplyLine.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo("applyHeaderId",dto.getInvoiceApplyLines());
        dto.setInvoiceApplyLines(invoiceApplyLineRepository.selectByCondition(condition));
        return dto;
    }
}

