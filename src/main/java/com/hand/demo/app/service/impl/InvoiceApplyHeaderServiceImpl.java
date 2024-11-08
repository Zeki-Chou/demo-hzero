package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.infra.constant.CodeRuleConstant;
import com.hand.demo.infra.constant.InvoiceApplyHeaderConstant;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import lombok.AllArgsConstructor;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author azhar.naufal@hand-global.com
 * @since 2024-11-04 10:11:56
 */
@Service
@AllArgsConstructor
public class InvoiceApplyHeaderServiceImpl implements InvoiceApplyHeaderService {
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    private InvoiceApplyLineService lineService;
    private LovAdapter lovAdapter;
    private CodeRuleBuilder codeRuleBuilder;
    private RedisHelper redisHelper;

    @Override
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader) {
        Page<InvoiceApplyHeader> headers;

        if (invoiceApplyHeader.getDelFlag() == null) {
            invoiceApplyHeader.setDelFlag(0);
        }

        if (invoiceApplyHeader.getDelFlag() != 1 && invoiceApplyHeader.getDelFlag() != 0) {
            throw new IllegalArgumentException("Del flag value must be 0 (not deleted) or 1 (deleted)");
        }

        headers = PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyHeaderRepository.selectList(invoiceApplyHeader));
        List<InvoiceApplyHeaderDTO> headerDTOs = headers.getContent().stream()
                .map(header -> {
                    InvoiceApplyHeaderDTO headerDTO = new InvoiceApplyHeaderDTO();
                    BeanUtils.copyProperties(header, headerDTO);
                    headerDTO.setInvoiceApplyLineList(lineService.linesByHeaderId(headerDTO.getApplyHeaderId()));
                    return headerDTO;
                })
                .collect(Collectors.toList());

        Page<InvoiceApplyHeaderDTO> headerDTOsPage = new Page<>();
        headerDTOsPage.setContent(headerDTOs);
        headerDTOsPage.setTotalPages(headers.getTotalPages());
        headerDTOsPage.setTotalElements(headers.getTotalElements());
        headerDTOsPage.setNumber(headers.getNumber());
        headerDTOsPage.setSize(headers.getSize());

        return headerDTOsPage;
    }

    @Override
    public InvoiceApplyHeader selectById(Long headerId){
        return invoiceApplyHeaderRepository.selectByPrimaryKey(headerId);
    }

    @Override
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public InvoiceApplyHeaderDTO detail(Long headerId){
        if(headerId == null){
            throw new CommonException("header Id must be filled");
        }

        String cacheKey = "headerKey_47361: " + headerId;

        if(redisHelper.strGet(cacheKey) == null){
            InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectByPrimary(headerId);
            if(header == null){
                throw new CommonException("Invoice Apply Header Not Found");
            }
            if(header.getDelFlag() == 1){
                throw new CommonException("Invoice Apply Header Was Deleted");
            }

            InvoiceApplyHeaderDTO headerDTO = new InvoiceApplyHeaderDTO();
            List<InvoiceApplyLine> lineList = lineService.linesByHeaderId(headerId);
            BeanUtils.copyProperties(header, headerDTO);
            headerDTO.setInvoiceApplyLineList(lineList);

            String jsonHeader = JSON.toJSONString(headerDTO);
            redisHelper.strSet(cacheKey, jsonHeader);
            return headerDTO;
        }

        InvoiceApplyHeaderDTO headerDTO = JSON.parseObject(redisHelper.strGet(cacheKey), InvoiceApplyHeaderDTO.class);
        return headerDTO;
    }

    @Override
    public InvoiceApplyHeader getHeaderById(Long headerId){
        return invoiceApplyHeaderRepository.selectByPrimary(headerId);
    }

    @Override
    public void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOS) {
        List<String> validApplyStatus = getValidLovValues(InvoiceApplyHeaderConstant.LovCode.APPLY_STATUS);
        List<String> validInvoiceType = getValidLovValues(InvoiceApplyHeaderConstant.LovCode.INVOICE_TYPE);
        List<String> validInvoiceColor = getValidLovValues(InvoiceApplyHeaderConstant.LovCode.INVOICE_COLOR);

        for (InvoiceApplyHeaderDTO invoiceDTO : invoiceApplyHeaderDTOS) {
            validateInvoiceDTO(invoiceDTO, validApplyStatus, validInvoiceType, validInvoiceColor);

            InvoiceApplyHeader savedHeader = saveOrUpdateHeader(invoiceDTO);

            List<InvoiceApplyLine> lines = invoiceDTO.getInvoiceApplyLineList();
            if (lines != null && !lines.isEmpty()) {
                for (InvoiceApplyLine line : lines) {
                    line.setApplyHeaderId(savedHeader.getApplyHeaderId());
                }
                lineService.saveData(lines);
            }
        }
    }

    private List<String> getValidLovValues(String lovCode) {
        return lovAdapter
                .queryLovValue(lovCode, BaseConstants.DEFAULT_TENANT_ID)
                .stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());
    }

    private void validateInvoiceDTO(InvoiceApplyHeaderDTO invoiceDTO, List<String> validApplyStatus,
                                    List<String> validInvoiceType, List<String> validInvoiceColor) {
        if (!validApplyStatus.contains(invoiceDTO.getApplyStatus())) {
            throw new CommonException(InvoiceApplyHeaderConstant.MESSAGE_ENCODING, "Apply Status: " + invoiceDTO.getApplyStatus() + " is Invalid");
        }
        if (!validInvoiceType.contains(invoiceDTO.getInvoiceType())) {
            throw new CommonException(InvoiceApplyHeaderConstant.MESSAGE_ENCODING, "Invoice Type: " + invoiceDTO.getInvoiceType() + " is Invalid");
        }
        if (!validInvoiceColor.contains(invoiceDTO.getInvoiceColor())) {
            throw new CommonException(InvoiceApplyHeaderConstant.MESSAGE_ENCODING, "Invoice Color: " + invoiceDTO.getInvoiceColor() + " is Invalid");
        }
    }

    private InvoiceApplyHeader saveOrUpdateHeader(InvoiceApplyHeaderDTO invoiceDTO) {
        InvoiceApplyHeader header = new InvoiceApplyHeader();
        BeanUtils.copyProperties(invoiceDTO, header);

        String cacheKey = "headerKey_47361: " + header.getApplyHeaderId();

        if (invoiceDTO.getApplyHeaderId() == null) {
            String batchCode = codeRuleBuilder.generateCode(CodeRuleConstant.CODE_RULE_HEADER_NUMBER, null);
            header.setApplyHeaderNumber(batchCode);
            invoiceApplyHeaderRepository.insertSelective(header);
        } else {
            invoiceApplyHeaderRepository.updateByPrimaryKeySelective(header);
            redisHelper.delKey(cacheKey);
        }

        return header;
    }


    @Override
    public void softDelete(Long applyHeaderId){
        InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(applyHeaderId);
        if(invoiceApplyHeader.getDelFlag() == 1){
            String errorMessage = "Invoice Header with number " + invoiceApplyHeader.getApplyHeaderNumber() + " already deleted";
            throw new CommonException(InvoiceApplyHeaderConstant.MESSAGE_ENCODING, errorMessage);
        }
        invoiceApplyHeader.setDelFlag(1);
        invoiceApplyHeaderRepository.updateByPrimaryKeySelective(invoiceApplyHeader);
    }

    @Override
    public void updateByPrimaryKeySelective(InvoiceApplyHeader header){
        invoiceApplyHeaderRepository.updateByPrimaryKeySelective(header);
    }
}

