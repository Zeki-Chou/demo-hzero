package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.InvoiceApplyConstants;
import com.hand.demo.infra.constant.TaskConstants;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.ext.IllegalArgumentException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import lombok.AllArgsConstructor;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.hzero.core.redis.RedisQueueHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author
 * @since 2024-11-04 10:14:05
 */
@Service
@AllArgsConstructor
public class InvoiceApplyHeaderServiceImpl implements InvoiceApplyHeaderService {
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    private InvoiceApplyLineService invoiceApplyLineService;

    private final LovAdapter lovAdapter;

    private final CodeRuleBuilder codeRuleBuilder;

    private RedisHelper redisHelper;

    @Override
//    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader) {
        Page<InvoiceApplyHeader> headers;

        if (invoiceApplyHeader.getDelFlag() == null) {
            invoiceApplyHeader.setDelFlag(0);
        }

        if (invoiceApplyHeader.getDelFlag() > 1) {
            throw new IllegalArgumentException("Del flag value must be 0 for not deleted invoice or 1 for deleted invoice");
        }

        if (invoiceApplyHeader.getDelFlag() == 1) {
            headers = PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyHeaderRepository.select("delFlag", 1));
        } else {
            headers = PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyHeaderRepository.select("delFlag", 0));
        }

        List<InvoiceApplyHeaderDTO> headerDTOs = headers.getContent().stream()
                .map(header -> {
                    InvoiceApplyHeaderDTO headerDTO = new InvoiceApplyHeaderDTO();
                    BeanUtils.copyProperties(header, headerDTO);
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
    public void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOs) {

        List<InvoiceApplyHeaderDTO> insertList = invoiceApplyHeaderDTOs.stream()
                .filter(header -> header.getApplyHeaderId() == null)
                .peek(header -> valueSetValidation(header.getApplyStatus(), header.getInvoiceType(), header.getInvoiceColor()))
                .collect(Collectors.toList());

        List<InvoiceApplyHeaderDTO> updateList = invoiceApplyHeaderDTOs.stream()
                .filter(header -> header.getApplyHeaderId() != null)
                .peek(header -> valueSetValidation(header.getApplyStatus(), header.getInvoiceType(), header.getInvoiceColor()))
                .collect(Collectors.toList());

        Map<String, String> variableMap = new HashMap<>();
        variableMap.put("customSegment", "-");

        List<String> batchCode = codeRuleBuilder.generateCode(insertList.size(), TaskConstants.CODE_RULE, variableMap);

        for (int i = 0; i < insertList.size(); i++) {
            InvoiceApplyHeaderDTO headerDTO = insertList.get(i);
            headerDTO.setApplyHeaderNumber(batchCode.get(i));
        }

        List<InvoiceApplyHeaderDTO> insertUpdateList = Stream.concat(insertList.stream(), updateList.stream())
                .collect(Collectors.toList());

        List<InvoiceApplyLine> invoiceApplyLines = new ArrayList<>();
        insertUpdateList.forEach(header -> {
            if(header.getApplyHeaderId() == null) {
                invoiceApplyHeaderRepository.insert(header);
            }
            Long applyHeaderId = header.getApplyHeaderId();
            List<InvoiceApplyLine> applyLines = header.getHeaderLines();

            if (applyLines != null) {
                applyLines.forEach(line -> {
                    line.setApplyHeaderId(applyHeaderId);
                    invoiceApplyLines.add(line);
                });
            }
        });

        if(!invoiceApplyLines.isEmpty()) {
            invoiceApplyLineService.saveData(invoiceApplyLines);
        }

        List<InvoiceApplyHeader> oriHeaderList = updateList.stream().map(headerDto -> {
            InvoiceApplyHeader iah = new InvoiceApplyHeader();
            BeanUtils.copyProperties(headerDto, iah);
            return iah;
        }).collect(Collectors.toList());

        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(oriHeaderList);
    }

    @Override
    public void softDeleteById(Long applyHeaderId) {
        invoiceApplyHeaderRepository.softDeleteById(applyHeaderId);
    }

    @Override
    public InvoiceApplyHeaderDTO detail(Long applyHeaderId) {
        String key = "invoiceDetail_47355";

        if(redisHelper.hasKey(key)) {
           if(redisHelper.strGet(key) != null && !redisHelper.strGet(key).isEmpty()) {
               System.out.println("alsjdnqwed: reddiss");
               return JSON.parseObject(redisHelper.strGet(key), InvoiceApplyHeaderDTO.class);
           }
        }

        InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(applyHeaderId);
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineRepository.select("applyHeaderId", applyHeaderId);

        InvoiceApplyHeaderDTO invoiceApplyHeaderDTO = new InvoiceApplyHeaderDTO();
        BeanUtils.copyProperties(invoiceApplyHeader, invoiceApplyHeaderDTO);
        invoiceApplyHeaderDTO.setHeaderLines(invoiceApplyLines);

        String headerJson = JSON.toJSONString(invoiceApplyHeaderDTO);
        redisHelper.strSet(key, headerJson);
        System.out.println("alsjdnqwed: no reddiss");

        return invoiceApplyHeaderDTO;
    }

    private void valueSetValidation(String applyStatus, String invoiceType, String invoiceColor) {
        List<String> allowedApplyStatuses = lovAdapter.queryLovValue(InvoiceApplyConstants.INV_APPLY_HEADER_APPLY_STATUS, BaseConstants.DEFAULT_TENANT_ID)
                .stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        List<String> allowedInvoiceColor = lovAdapter.queryLovValue(InvoiceApplyConstants.INV_APPLY_HEADER_INV_COLOR, BaseConstants.DEFAULT_TENANT_ID)
                .stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        List<String> allowedInvoiceType = lovAdapter.queryLovValue(InvoiceApplyConstants.INV_APPLY_HEADER_INV_TYPE, BaseConstants.DEFAULT_TENANT_ID)
                .stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        if (!allowedApplyStatuses.contains(applyStatus)) {
            throw new CommonException("Apply status not valid");
        }

        if (!allowedInvoiceType.contains(invoiceType)) {
            throw new CommonException("Invoice type not valid");
        }

        if(!allowedInvoiceColor.contains(invoiceColor)) {
            throw new CommonException("Invoice color not valid");
        }
    }
}

