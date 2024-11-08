package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.InvoiceApplyHeaderConstant;
import com.netflix.discovery.converters.Auto;
import io.choerodon.core.domain.Page;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author
 * @since 2024-11-04 10:16:08
 */
@Service
public class InvoiceApplyHeaderServiceImpl implements InvoiceApplyHeaderService {
    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    private final LovAdapter lovAdapter;

    @Autowired
    private final CodeRuleBuilder codeRuleBuilder;

    @Autowired
    RedisHelper redisHelper;

    public InvoiceApplyHeaderServiceImpl(InvoiceApplyHeaderRepository invoiceApplyHeaderRepository, LovAdapter lovAdapter, CodeRuleBuilder codeRuleBuilder) {
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
        this.lovAdapter = lovAdapter;
        this.codeRuleBuilder = codeRuleBuilder;
    }

    @Override
    public Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader) {
        Page<InvoiceApplyHeader> pageResult = PageHelper.doPageAndSort(pageRequest, () -> {
            if (invoiceApplyHeader.getDelFlag() == null || invoiceApplyHeader.getDelFlag() == 0) {
                invoiceApplyHeader.setDelFlag(0);
                return invoiceApplyHeaderRepository.selectList(invoiceApplyHeader);
            } else {
                invoiceApplyHeader.setDelFlag(1);
                return invoiceApplyHeaderRepository.selectList(invoiceApplyHeader);
            }
        });

        List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOList = new ArrayList<>();

        for (InvoiceApplyHeader data : pageResult.getContent()) {
            invoiceApplyHeaderDTOList.add(convertToDTO(data));
        }

        Page<InvoiceApplyHeaderDTO> dtoPage = new Page<>();
        dtoPage.setContent(invoiceApplyHeaderDTOList);
        dtoPage.setTotalPages(pageResult.getTotalPages());
        dtoPage.setTotalElements(pageResult.getTotalElements());
        dtoPage.setNumber(pageResult.getNumber());
        dtoPage.setSize(pageResult.getSize());
        return dtoPage;
    }

    @Override
    public void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders) {
        List<LovValueDTO> countInvoiceType = lovAdapter.queryLovValue(InvoiceApplyHeaderConstant.INVOICE_TYPE, BaseConstants.DEFAULT_TENANT_ID);
        List<LovValueDTO> countInvoiceColor = lovAdapter.queryLovValue(InvoiceApplyHeaderConstant.INVOICE_COLOR, BaseConstants.DEFAULT_TENANT_ID);
        List<LovValueDTO> countApplyStatus = lovAdapter.queryLovValue(InvoiceApplyHeaderConstant.APPLY_STATUS, BaseConstants.DEFAULT_TENANT_ID);

        List<String> invoiceType = countInvoiceType.stream().map(LovValueDTO::getValue).collect(Collectors.toList());
        List<String> invoiceColor = countInvoiceColor.stream().map(LovValueDTO::getValue).collect(Collectors.toList());
        List<String> applyStatus = countApplyStatus.stream().map(LovValueDTO::getValue).collect(Collectors.toList());

        List<String> validationError = new ArrayList<>();

        for(int i = 0; i < invoiceApplyHeaders.size(); i++) {
            InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaders.get(i);
            if(!invoiceType.contains(invoiceApplyHeader.getInvoiceType())) {
                validationError.add("Error Invoice Type : " + invoiceApplyHeader.getInvoiceType());
            }

            if(!invoiceColor.contains(invoiceApplyHeader.getInvoiceColor())) {
                validationError.add("Error Invoice Color : " + invoiceApplyHeader.getInvoiceColor());
            }

            if(!applyStatus.contains(invoiceApplyHeader.getApplyStatus())) {
                validationError.add("Error Apply Status : " + invoiceApplyHeader.getApplyStatus());
            }
        }

        if (!validationError.isEmpty()) {
            throw new IllegalArgumentException(validationError.toString());
        }

        List<InvoiceApplyHeaderDTO> insertListDTO = invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() == null).collect(Collectors.toList());

        List<String> batchCode = codeRuleBuilder.generateCode(insertListDTO.size(), InvoiceApplyHeaderConstant.INVOICE_HEADER, null);
        for (int i = 0; i < insertListDTO.size(); i++) {
            InvoiceApplyHeader invoiceApplyHeader = insertListDTO.get(i);
            invoiceApplyHeader.setApplyHeaderNumber(batchCode.get(i));

            BigDecimal headerTaxAmount = BigDecimal.ZERO;
            BigDecimal headerExcludeTaxAmount = BigDecimal.ZERO;
            BigDecimal headerTotalAmount = BigDecimal.ZERO;

            InvoiceApplyHeaderDTO invoiceApplyHeaderDTO = insertListDTO.get(i);
            List<InvoiceApplyLine> invoiceApplyLineList = invoiceApplyHeaderDTO.getInvoiceApplyLines();

            for (int p = 0; p < invoiceApplyLineList.size(); p++) {
                InvoiceApplyLine invoiceApplyLine = invoiceApplyLineList.get(p);

                BigDecimal totalAmount = invoiceApplyLine.getUnitPrice().multiply(invoiceApplyLine.getQuantity());
                BigDecimal taxAmount = totalAmount.multiply(invoiceApplyLine.getTaxRate());
                BigDecimal excludeTaxAmount = totalAmount.subtract(taxAmount);

                headerTaxAmount = headerTaxAmount.add(taxAmount);
                headerExcludeTaxAmount = headerExcludeTaxAmount.add(excludeTaxAmount);
                headerTotalAmount = headerTotalAmount.add(totalAmount);
            }

            invoiceApplyHeader.setTotalAmount(headerTotalAmount);
            invoiceApplyHeader.setTaxAmount(headerTaxAmount);
            invoiceApplyHeader.setExcludeTaxAmount(headerExcludeTaxAmount);
            invoiceApplyHeader.setDelFlag(0);

            invoiceApplyHeaderRepository.insert(invoiceApplyHeader);

            if(invoiceApplyLineList.size() > 0) {
                Long headerId = invoiceApplyHeader.getApplyHeaderId();

                for(int c = 0; c < invoiceApplyLineList.size(); c++) {
                    BigDecimal taxAmount = BigDecimal.ZERO;
                    BigDecimal excludeTaxAmount = BigDecimal.ZERO;
                    BigDecimal totalAmount = BigDecimal.ZERO;

                    InvoiceApplyLine invoiceApplyLine = invoiceApplyLineList.get(c);
                    totalAmount = invoiceApplyLine.getUnitPrice().multiply(invoiceApplyLine.getQuantity());
                    taxAmount = totalAmount.multiply(invoiceApplyLine.getTaxRate());
                    excludeTaxAmount = totalAmount.subtract(taxAmount);

                    invoiceApplyLine.setTotalAmount(totalAmount);
                    invoiceApplyLine.setTaxAmount(taxAmount);
                    invoiceApplyLine.setExcludeTaxAmount(excludeTaxAmount);

                    invoiceApplyLine.setApplyHeaderId(headerId);
                    invoiceApplyLineRepository.insertSelective(invoiceApplyLine);
                }
            }
        }

        List<InvoiceApplyHeaderDTO> updateListDTO = invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() != null).collect(Collectors.toList());
        for(int i = 0; i < updateListDTO.size(); i++) {
            InvoiceApplyHeader invoiceApplyHeader = updateListDTO.get(i);
            InvoiceApplyHeader invoiceApplyHeader1 = invoiceApplyHeaderRepository.selectByPrimary(invoiceApplyHeader.getApplyHeaderId());
            invoiceApplyHeader.setObjectVersionNumber(invoiceApplyHeader1.getObjectVersionNumber());
            invoiceApplyHeader.setDelFlag(0);

            InvoiceApplyHeaderDTO invoiceApplyHeaderDTO = updateListDTO.get(i);

            List<InvoiceApplyLine> invoiceApplyLineList = invoiceApplyHeaderDTO.getInvoiceApplyLines();
            for(int k = 0; k < invoiceApplyLineList.size(); k++) {
                InvoiceApplyLine invoiceApplyLine = invoiceApplyLineList.get(k);

                InvoiceApplyLine invoiceApplyLineNew = new InvoiceApplyLine();
                invoiceApplyLineNew.setApplyHeaderId(invoiceApplyHeaderDTO.getApplyHeaderId());
                invoiceApplyLineNew.setApplyLineId(invoiceApplyLine.getApplyLineId());

                List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineRepository.select(invoiceApplyLineNew);

                BigDecimal taxAmount = BigDecimal.ZERO;
                BigDecimal totalAmount = BigDecimal.ZERO;
                BigDecimal excludeTaxAmount = BigDecimal.ZERO;

                if(invoiceApplyLines.size() > 0) {
                    InvoiceApplyLine invoiceApplyLine1 = invoiceApplyLines.get(0);
                    invoiceApplyLine.setObjectVersionNumber(invoiceApplyLine1.getObjectVersionNumber());
                    invoiceApplyLine.setApplyHeaderId(invoiceApplyHeaderDTO.getApplyHeaderId());

                    totalAmount = invoiceApplyLine.getUnitPrice().multiply(invoiceApplyLine.getQuantity());
                    taxAmount = totalAmount.multiply(invoiceApplyLine.getTaxRate());
                    excludeTaxAmount = totalAmount.subtract(taxAmount);

                    invoiceApplyLine.setTotalAmount(totalAmount);
                    invoiceApplyLine.setTaxAmount(taxAmount);
                    invoiceApplyLine.setExcludeTaxAmount(excludeTaxAmount);

                    invoiceApplyLineRepository.updateByPrimaryKeySelective(invoiceApplyLine);
                } else {
                    totalAmount = invoiceApplyLine.getUnitPrice().multiply(invoiceApplyLine.getQuantity());
                    taxAmount = totalAmount.multiply(invoiceApplyLine.getTaxRate());
                    excludeTaxAmount = totalAmount.subtract(taxAmount);

                    invoiceApplyLine.setTotalAmount(totalAmount);
                    invoiceApplyLine.setTaxAmount(taxAmount);
                    invoiceApplyLine.setExcludeTaxAmount(excludeTaxAmount);

                    invoiceApplyLineRepository.insertSelective(invoiceApplyLine);
                }
            }

            InvoiceApplyLine invoiceApplyLineNew = new InvoiceApplyLine();
            invoiceApplyLineNew.setApplyHeaderId(invoiceApplyHeader.getApplyHeaderId());

            List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineRepository.select(invoiceApplyLineNew);

            BigDecimal headerTaxAmount = BigDecimal.ZERO;
            BigDecimal headerExcludeTaxAmount = BigDecimal.ZERO;
            BigDecimal headerTotalAmount = BigDecimal.ZERO;

            for(int p = 0; p < invoiceApplyLines.size(); p++) {
                InvoiceApplyLine invoiceApplyLine = invoiceApplyLines.get(p);
                BigDecimal taxAmount = invoiceApplyLine.getTaxAmount() != null ? invoiceApplyLine.getTaxAmount() : BigDecimal.ZERO;
                BigDecimal excludeTaxAmount = invoiceApplyLine.getExcludeTaxAmount() != null ? invoiceApplyLine.getExcludeTaxAmount() : BigDecimal.ZERO;
                BigDecimal totalAmount = invoiceApplyLine.getTotalAmount() != null ? invoiceApplyLine.getTotalAmount() : BigDecimal.ZERO;

                headerTaxAmount = headerTaxAmount.add(taxAmount);
                headerExcludeTaxAmount = headerExcludeTaxAmount.add(excludeTaxAmount);
                headerTotalAmount = headerTotalAmount.add(totalAmount);
            }

            invoiceApplyHeader.setTaxAmount(headerTaxAmount);
            invoiceApplyHeader.setExcludeTaxAmount(headerExcludeTaxAmount);
            invoiceApplyHeader.setTotalAmount(headerTotalAmount);
            invoiceApplyHeaderRepository.updateByPrimaryKeySelective(invoiceApplyHeader);
        }
    }

    public InvoiceApplyHeaderDTO detail(Long headerId) {

        String result = redisHelper.strGet("Andrew_" + headerId.toString());
        if (result != null && !result.isEmpty()) {
            return JSON.parseObject(result, InvoiceApplyHeaderDTO.class);
        }

        InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(headerId);
        InvoiceApplyHeaderDTO invoiceApplyHeaderDTO = new InvoiceApplyHeaderDTO();

        InvoiceApplyLine invoiceApplyLine = new InvoiceApplyLine();
        invoiceApplyLine.setApplyHeaderId(headerId);

        List<InvoiceApplyLine> invoiceApplyLineList = invoiceApplyLineRepository.select(invoiceApplyLine);
        invoiceApplyHeaderDTO.setInvoiceApplyLines(invoiceApplyLineList);

        BeanUtils.copyProperties(invoiceApplyHeader, invoiceApplyHeaderDTO);

        String serializeDTO = JSON.toJSONString(invoiceApplyHeaderDTO);
        redisHelper.strSet("Andrew_" + headerId.toString(), serializeDTO);

        return invoiceApplyHeaderDTO;
    }

    public void deleteData(Long headerId) {
        InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(headerId);
        invoiceApplyHeader.setDelFlag(1);
        invoiceApplyHeaderRepository.updateByPrimaryKeySelective(invoiceApplyHeader);
    }

    private InvoiceApplyHeaderDTO convertToDTO(InvoiceApplyHeader invoiceApplyHeader) {
        InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();
        BeanUtils.copyProperties(invoiceApplyHeader, dto);

        InvoiceApplyLine invoiceApplyLine = new InvoiceApplyLine();
        invoiceApplyLine.setApplyHeaderId(invoiceApplyHeader.getApplyHeaderId());
        List<InvoiceApplyLine> invoiceApplyLineList = invoiceApplyLineRepository.select(invoiceApplyLine);

        dto.setInvoiceApplyLines(invoiceApplyLineList);

        return dto;
    }

    public List<InvoiceApplyHeaderDTO> exportAll (PageRequest pageRequest) {
        return PageHelper.doPage(pageRequest, ()->invoiceApplyHeaderRepository.selectAll());
    }
}