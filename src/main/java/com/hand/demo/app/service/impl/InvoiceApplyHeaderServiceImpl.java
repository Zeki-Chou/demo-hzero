package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.InvoiceApplyHeaderConstant;
import com.hand.demo.infra.util.Utils;
import com.netflix.discovery.converters.Auto;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.message.MessageAccessor;
import org.hzero.core.redis.RedisHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.*;
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
    public Page<InvoiceApplyHeader> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader) {
        Page<InvoiceApplyHeader> pageResult = PageHelper.doPageAndSort(pageRequest, () -> {
            if (invoiceApplyHeader.getDelFlag() == null || invoiceApplyHeader.getDelFlag() == 0) {
                invoiceApplyHeader.setDelFlag(0);
            }
            return invoiceApplyHeaderRepository.selectList(invoiceApplyHeader);
        });

        Page<InvoiceApplyHeader> invoiceApplyHeadersPage = new Page<>();
        invoiceApplyHeadersPage.setContent(pageResult.getContent());
        invoiceApplyHeadersPage.setTotalPages(pageResult.getTotalPages());
        invoiceApplyHeadersPage.setTotalElements(pageResult.getTotalElements());
        invoiceApplyHeadersPage.setNumber(pageResult.getNumber());
        invoiceApplyHeadersPage.setSize(pageResult.getSize());

        return invoiceApplyHeadersPage;
    }

    @Override
    public void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders) {
//        validate error
        List<LovValueDTO> countInvoiceType = lovAdapter.queryLovValue(InvoiceApplyHeaderConstant.INVOICE_TYPE, BaseConstants.DEFAULT_TENANT_ID);
        List<LovValueDTO> countInvoiceColor = lovAdapter.queryLovValue(InvoiceApplyHeaderConstant.INVOICE_COLOR, BaseConstants.DEFAULT_TENANT_ID);
        List<LovValueDTO> countApplyStatus = lovAdapter.queryLovValue(InvoiceApplyHeaderConstant.APPLY_STATUS, BaseConstants.DEFAULT_TENANT_ID);

        List<String> invoiceType = countInvoiceType.stream().map(LovValueDTO::getValue).collect(Collectors.toList());
        List<String> invoiceColor = countInvoiceColor.stream().map(LovValueDTO::getValue).collect(Collectors.toList());
        List<String> applyStatus = countApplyStatus.stream().map(LovValueDTO::getValue).collect(Collectors.toList());

        List<String> validationError = new ArrayList<>();

        for(int i = 0; i < invoiceApplyHeaders.size(); i++) {
            InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaders.get(i);

            if(invoiceApplyHeader.getApplyHeaderId() != null) {
                InvoiceApplyHeader invoiceApplyHeaderNew = invoiceApplyHeaderRepository.selectByPrimary(invoiceApplyHeader.getApplyHeaderId());
                if(invoiceApplyHeaderNew == null) {
                    validationError.add("header_id does not exist" + "{" + i + "}");
                }
            }

            if(!invoiceType.contains(invoiceApplyHeader.getInvoiceType())) {
                validationError.add("Error Invoice Type"+"{"+ i +"}"+" : " + invoiceApplyHeader.getInvoiceType());
            }

            if(!invoiceColor.contains(invoiceApplyHeader.getInvoiceColor())) {
                validationError.add("Error Invoice Color"+"{"+ i +"}"+" : " + invoiceApplyHeader.getInvoiceColor());
            }

            if(!applyStatus.contains(invoiceApplyHeader.getApplyStatus())) {
                validationError.add("Error Apply Status :"+"{"+ i +"}"+" " + invoiceApplyHeader.getApplyStatus());
            }
        }

        if (!validationError.isEmpty()) {
//            throw new IllegalArgumentException(validationError.toString());
//            Object[] errorMsgArgs = new Object[] {validationError.toString()};
//            String error = MessageAccessor.getMessage(InvoiceApplyHeaderConstant.MULTILINGUAL_HEADER_ID, errorMsgArgs, Locale.CHINESE).getDesc();
            throw new CommonException("exam-47356.apply-header.error", validationError.toString());
        }

//        insert DTO
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

//count tax, total, and exclude amount from request
                BigDecimal totalAmount = invoiceApplyLine.getUnitPrice().multiply(invoiceApplyLine.getQuantity());
                BigDecimal taxAmount = totalAmount.multiply(invoiceApplyLine.getTaxRate());
                BigDecimal excludeTaxAmount = totalAmount.subtract(taxAmount);

                headerTaxAmount = headerTaxAmount.add(taxAmount);
                headerExcludeTaxAmount = headerExcludeTaxAmount.add(excludeTaxAmount);
                headerTotalAmount = headerTotalAmount.add(totalAmount);
            }

//            set it to invoice ApplyHeader
            invoiceApplyHeader.setTotalAmount(headerTotalAmount);
            invoiceApplyHeader.setTaxAmount(headerTaxAmount);
            invoiceApplyHeader.setExcludeTaxAmount(headerExcludeTaxAmount);
            invoiceApplyHeader.setDelFlag(0);

            invoiceApplyHeaderRepository.insert(invoiceApplyHeader);

//            count total, tax, exclude on invoiceApplyLine and update it to invoiceApplyHeader
            if(!invoiceApplyLineList.isEmpty()) {
                Long headerId = invoiceApplyHeader.getApplyHeaderId();

                for (InvoiceApplyLine applyLine : invoiceApplyLineList) {
                    BigDecimal taxAmount = BigDecimal.ZERO;
                    BigDecimal excludeTaxAmount = BigDecimal.ZERO;
                    BigDecimal totalAmount = BigDecimal.ZERO;

                    InvoiceApplyLine invoiceApplyLine = applyLine;
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

//        code for update
        List<InvoiceApplyHeaderDTO> updateListDTO = invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() != null).collect(Collectors.toList());
        for(int i = 0; i < updateListDTO.size(); i++) {
            InvoiceApplyHeader invoiceApplyHeader = updateListDTO.get(i);
            InvoiceApplyHeader invoiceApplyHeader1 = invoiceApplyHeaderRepository.selectByPrimary(invoiceApplyHeader.getApplyHeaderId());
            invoiceApplyHeader.setObjectVersionNumber(invoiceApplyHeader1.getObjectVersionNumber());
            invoiceApplyHeader.setDelFlag(0);

            InvoiceApplyHeaderDTO invoiceApplyHeaderDTO = updateListDTO.get(i);

            invoiceApplyHeaderRepository.updateByPrimaryKeySelective(invoiceApplyHeader);

            List<InvoiceApplyLine> invoiceApplyLineList = invoiceApplyHeaderDTO.getInvoiceApplyLines();
            for(int k = 0; k < invoiceApplyLineList.size(); k++) {
                InvoiceApplyLine invoiceApplyLine = invoiceApplyLineList.get(k);

//                validate first
                InvoiceApplyLine invoiceApplyLineNew = new InvoiceApplyLine();
                invoiceApplyLineNew.setApplyHeaderId(invoiceApplyHeaderDTO.getApplyHeaderId());

                if(invoiceApplyLine.getApplyLineId() == null) {
                    invoiceApplyLineNew.setApplyLineId(0L);
                } else {
                    invoiceApplyLineNew.setApplyLineId(invoiceApplyLine.getApplyLineId());
                }

//                check if invoiceApplyLineList, header and apply line id is on database
                List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineRepository.select(invoiceApplyLineNew);

                BigDecimal taxAmount = BigDecimal.ZERO;
                BigDecimal totalAmount = BigDecimal.ZERO;
                BigDecimal excludeTaxAmount = BigDecimal.ZERO;
// if exist in database, do update
                if(invoiceApplyLines.size() > 0) {
                    InvoiceApplyLine invoiceApplyLine1 = invoiceApplyLines.get(0);
                    invoiceApplyLine.setObjectVersionNumber(invoiceApplyLine1.getObjectVersionNumber());
//  set apply header id based on request
                    invoiceApplyLine.setApplyHeaderId(invoiceApplyHeaderDTO.getApplyHeaderId());

// count total, tax, and exclude
                    totalAmount = invoiceApplyLine.getUnitPrice().multiply(invoiceApplyLine.getQuantity());
                    taxAmount = totalAmount.multiply(invoiceApplyLine.getTaxRate());
                    excludeTaxAmount = totalAmount.subtract(taxAmount);

                    invoiceApplyLine.setTotalAmount(totalAmount);
                    invoiceApplyLine.setTaxAmount(taxAmount);
                    invoiceApplyLine.setExcludeTaxAmount(excludeTaxAmount);

//              do update
                    invoiceApplyLineRepository.updateByPrimaryKeySelective(invoiceApplyLine);
                } else {
//  if not exist in database, insert line
                    totalAmount = invoiceApplyLine.getUnitPrice().multiply(invoiceApplyLine.getQuantity());
                    taxAmount = totalAmount.multiply(invoiceApplyLine.getTaxRate());
                    excludeTaxAmount = totalAmount.subtract(taxAmount);

                    invoiceApplyLine.setApplyHeaderId(invoiceApplyHeader.getApplyHeaderId());
                    invoiceApplyLine.setTotalAmount(totalAmount);
                    invoiceApplyLine.setTaxAmount(taxAmount);
                    invoiceApplyLine.setExcludeTaxAmount(excludeTaxAmount);
//  do insert
                    invoiceApplyLineRepository.insertSelective(invoiceApplyLine);
                }
            }

//  count tax, exlude, total amount all in invoice apply line and update it
            countApplyLineUpdateHeader(invoiceApplyHeader.getApplyHeaderId());
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

    public List<InvoiceApplyHeaderDTO> exportAll (PageRequest pageRequest) {
        return PageHelper.doPage(pageRequest, ()->invoiceApplyHeaderRepository.selectAll());
    }

    public void countApplyLineUpdateHeader (Long header_id) {
        InvoiceApplyLine invoiceApplyLineNew = new InvoiceApplyLine();
        invoiceApplyLineNew.setApplyHeaderId(header_id);

        List<InvoiceApplyLine> invoiceApplyLineList = invoiceApplyLineRepository.select(invoiceApplyLineNew);
        BigDecimal headerTaxAmount = BigDecimal.ZERO;
        BigDecimal headerExcludeTaxAmount = BigDecimal.ZERO;
        BigDecimal headerTotalAmount = BigDecimal.ZERO;

        for(InvoiceApplyLine invoiceApplyLine : invoiceApplyLineList) {
            BigDecimal taxAmount = invoiceApplyLine.getTaxAmount() != null ? invoiceApplyLine.getTaxAmount() : BigDecimal.ZERO;
            BigDecimal excludeTaxAmount = invoiceApplyLine.getExcludeTaxAmount() != null ? invoiceApplyLine.getExcludeTaxAmount() : BigDecimal.ZERO;
            BigDecimal totalAmount = invoiceApplyLine.getTotalAmount() != null ? invoiceApplyLine.getTotalAmount() : BigDecimal.ZERO;

            headerTaxAmount = headerTaxAmount.add(taxAmount);
            headerExcludeTaxAmount = headerExcludeTaxAmount.add(excludeTaxAmount);
            headerTotalAmount = headerTotalAmount.add(totalAmount);
        }

        InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(header_id);

        InvoiceApplyHeader invoiceApplyHeaderNew = new InvoiceApplyHeader();
        invoiceApplyHeaderNew.setApplyHeaderId(header_id);
        invoiceApplyHeaderNew.setTaxAmount(headerTaxAmount);
        invoiceApplyHeaderNew.setExcludeTaxAmount(headerExcludeTaxAmount);
        invoiceApplyHeaderNew.setTotalAmount(headerTotalAmount);
        invoiceApplyHeaderNew.setObjectVersionNumber(invoiceApplyHeader.getObjectVersionNumber());

        invoiceApplyHeaderRepository.updateByPrimaryKeySelective(invoiceApplyHeaderNew);
    }
}