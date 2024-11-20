package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.dto.InvoiceApplyInfoDTO;
import com.hand.demo.api.dto.InvoiceApplyInfoDTOOut;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.domain.repository.UserRepository;
import com.hand.demo.infra.constant.InvoiceApplyHeaderConstant;
import com.hand.demo.infra.mapper.UserMapper;
import com.hand.demo.infra.util.Utils;
import com.netflix.discovery.converters.Auto;
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
import org.hzero.core.base.BaseConstants;
import org.hzero.core.message.MessageAccessor;
import org.hzero.core.redis.RedisHelper;
import org.hzero.mybatis.domian.Condition;
import org.json.JSONObject;
import org.opensaml.xml.signature.P;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.http.ResponseEntity;
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
    private UserRepository userRepository;

    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    private final LovAdapter lovAdapter;

    @Autowired
    private final CodeRuleBuilder codeRuleBuilder;

    @Autowired
    RedisHelper redisHelper;

    @Autowired
    IamRemoteService iamRemoteService;

    public InvoiceApplyHeaderServiceImpl(InvoiceApplyHeaderRepository invoiceApplyHeaderRepository, LovAdapter lovAdapter, CodeRuleBuilder codeRuleBuilder) {
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
        this.lovAdapter = lovAdapter;
        this.codeRuleBuilder = codeRuleBuilder;
    }

    @Override
    public Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeaderDTO invoiceApplyHeader) {
        // dto pasang admin flag
        JSONObject jsonObject = new JSONObject(iamRemoteService.selectSelf().getBody());

        Boolean tenantAdminFlag;
        if(jsonObject.has("tenantAdminFlag")) {
            tenantAdminFlag = jsonObject.getBoolean("tenantAdminFlag");
        } else {
            tenantAdminFlag = false;
        }

        invoiceApplyHeader.setTenantAdminFlag(tenantAdminFlag);

        Page<InvoiceApplyHeader> pageResult = PageHelper.doPageAndSort(pageRequest, () -> {
            if (invoiceApplyHeader.getDelFlag() == null || invoiceApplyHeader.getDelFlag() == 0) {
                invoiceApplyHeader.setDelFlag(0);
            }
            return invoiceApplyHeaderRepository.selectList(invoiceApplyHeader);
        });

        List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOList = new ArrayList<>();
        for (InvoiceApplyHeader data : pageResult) {
            InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();
            BeanUtils.copyProperties(data, dto);
            invoiceApplyHeaderDTOList.add(dto);
        }

        Page<InvoiceApplyHeaderDTO> invoiceApplyHeadersDTOPage = new Page<>();
        invoiceApplyHeadersDTOPage.setContent(invoiceApplyHeaderDTOList);
        invoiceApplyHeadersDTOPage.setTotalPages(pageResult.getTotalPages());
        invoiceApplyHeadersDTOPage.setTotalElements(pageResult.getTotalElements());
        invoiceApplyHeadersDTOPage.setNumber(pageResult.getNumber());
        invoiceApplyHeadersDTOPage.setSize(pageResult.getSize());

        return invoiceApplyHeadersDTOPage;
    }

    public void invoiceApplySetData (InvoiceApplyInfoDTO invoiceApplyInfoDTO) {
        if(invoiceApplyInfoDTO.getCreationDateFrom() == null && invoiceApplyInfoDTO.getCreationDateTo() != null) {
            invoiceApplyInfoDTO.setCreationDateFrom(new Date());
        }

        if(invoiceApplyInfoDTO.getSubmitTimeFrom() == null && invoiceApplyInfoDTO.getSubmitTimeTo() != null) {
            invoiceApplyInfoDTO.setSubmitTimeFrom(new Date());
        }
    }

    public InvoiceApplyInfoDTO invoiceApplyInfoDetail (InvoiceApplyInfoDTO invoiceApplyInfoDTO) {
        invoiceApplySetData(invoiceApplyInfoDTO);

        List<LovValueDTO> countInvoiceType = lovAdapter.queryLovValue(InvoiceApplyHeaderConstant.INVOICE_TYPE, BaseConstants.DEFAULT_TENANT_ID);
        List<LovValueDTO> countApplyStatus = lovAdapter.queryLovValue(InvoiceApplyHeaderConstant.APPLY_STATUS, BaseConstants.DEFAULT_TENANT_ID);

        Map<String, String> applyStatusMapValueToKey = countApplyStatus.stream()
                .collect(Collectors.toMap(LovValueDTO::getMeaning, LovValueDTO::getValue));

        Map<String, String> invoiceTypeMapValueToKey = countInvoiceType.stream()
                .collect(Collectors.toMap(LovValueDTO::getMeaning, LovValueDTO::getValue));

        String invoiceTypeMeaning = "";

        if (invoiceTypeMapValueToKey.containsKey(invoiceApplyInfoDTO.getInvoiceType())) {
            invoiceTypeMeaning = invoiceApplyInfoDTO.getInvoiceType();
        }

        List<String> applyStatus = invoiceApplyInfoDTO.getApplyStatusList();
        List<String> applyStatusKeys = new ArrayList<>();
        List<String> applyStatusMeanings = new ArrayList<>();

        if(applyStatus != null) {
            for (String key : applyStatus) {
                if (applyStatusMapValueToKey.containsKey(key)) {
                    applyStatusKeys.add(applyStatusMapValueToKey.get(key));
                    applyStatusMeanings.add(key);
                }
            }

            invoiceApplyInfoDTO.setApplyStatusList(applyStatusKeys);
        }

        if(invoiceApplyInfoDTO.getInvoiceType() != null) {
            invoiceApplyInfoDTO.setInvoiceType(invoiceTypeMapValueToKey.get(invoiceApplyInfoDTO.getInvoiceType()));
        }

        List<InvoiceApplyInfoDTOOut> invoiceApplyInfoDTOOut = invoiceApplyHeaderRepository.invoiceApplyInfo(invoiceApplyInfoDTO);

        invoiceApplyInfoDTO.setInvoiceApplyInfoDTOOut(invoiceApplyInfoDTOOut);

        if(invoiceApplyInfoDTO.getInvoiceType() != null) {
            invoiceApplyInfoDTO.setInvoiceType(invoiceTypeMeaning);
        }
        if(applyStatus != null) {
            invoiceApplyInfoDTO.setApplyStatusMeaning(applyStatusMeanings.toString().replace("[", "").replace("]", ""));
        }

        JSONObject jsonObject = new JSONObject(iamRemoteService.selectSelf().getBody());
        invoiceApplyInfoDTO.setTenantName(jsonObject.getString("tenantName"));
        return invoiceApplyInfoDTO;
    }

    public void validateDTO(List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOList) {
        List<LovValueDTO> countInvoiceType = lovAdapter.queryLovValue(InvoiceApplyHeaderConstant.INVOICE_TYPE, BaseConstants.DEFAULT_TENANT_ID);
        List<LovValueDTO> countInvoiceColor = lovAdapter.queryLovValue(InvoiceApplyHeaderConstant.INVOICE_COLOR, BaseConstants.DEFAULT_TENANT_ID);
        List<LovValueDTO> countApplyStatus = lovAdapter.queryLovValue(InvoiceApplyHeaderConstant.APPLY_STATUS, BaseConstants.DEFAULT_TENANT_ID);

        List<String> invoiceType = countInvoiceType.stream().map(LovValueDTO::getValue).collect(Collectors.toList());
        List<String> invoiceColor = countInvoiceColor.stream().map(LovValueDTO::getValue).collect(Collectors.toList());
        List<String> applyStatus = countApplyStatus.stream().map(LovValueDTO::getValue).collect(Collectors.toList());

        List<String> validationError = new ArrayList<>();
        for(int i = 0; i < invoiceApplyHeaderDTOList.size(); i++) {
            InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderDTOList.get(i);

//          if header is not null, then it will validate
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
                validationError.add("Error Apply Status"+"{"+ i +"}"+" : " + invoiceApplyHeader.getApplyStatus());
            }
        }

        if (!validationError.isEmpty()) {
//            throw new IllegalArgumentException(validationError.toString());
//            Object[] errorMsgArgs = new Object[] {validationError.toString()};
//            String error = MessageAccessor.getMessage(InvoiceApplyHeaderConstant.MULTILINGUAL_HEADER_ID, errorMsgArgs, Locale.CHINESE).getDesc();
            throw new CommonException("exam-47356.apply-header.error", validationError.toString());
        }
    }

    @Override
    public void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOList) {
//      validate data first
        validateDTO(invoiceApplyHeaderDTOList);

//      seperated code for insert and update based on headerIs is exist or not
//      code for insert DTO
        List<InvoiceApplyHeaderDTO> insertListDTO = invoiceApplyHeaderDTOList.stream().filter(line -> line.getApplyHeaderId() == null).collect(Collectors.toList());
        List<String> batchCode = codeRuleBuilder.generateCode(insertListDTO.size(), InvoiceApplyHeaderConstant.INVOICE_HEADER, null);
        for (int i = 0; i < insertListDTO.size(); i++) {
            InvoiceApplyHeader invoiceApplyHeader = insertListDTO.get(i);
            invoiceApplyHeader.setApplyHeaderNumber(batchCode.get(i));

            BigDecimal headerTaxAmount = BigDecimal.ZERO;
            BigDecimal headerExcludeTaxAmount = BigDecimal.ZERO;
            BigDecimal headerTotalAmount = BigDecimal.ZERO;

            InvoiceApplyHeaderDTO invoiceApplyHeaderDTO = insertListDTO.get(i);
            List<InvoiceApplyLine> invoiceApplyLineList = invoiceApplyHeaderDTO.getInvoiceApplyLines();

//            code to count all amount on apply line list
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

//            insert apply header that already counted
            invoiceApplyHeaderRepository.insert(invoiceApplyHeader);

//            code for insert apply line
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
        List<InvoiceApplyHeaderDTO> updateListDTO = invoiceApplyHeaderDTOList.stream().filter(line -> line.getApplyHeaderId() != null).collect(Collectors.toList());
        List<InvoiceApplyLine> invoiceApplyLineUpdate = new ArrayList<>();
        List<InvoiceApplyLine> invoiceApplyLineInsert = new ArrayList<>();

        for(int i = 0; i < updateListDTO.size(); i++) {
//          code to set object version number
            InvoiceApplyHeader invoiceApplyHeader = updateListDTO.get(i);
            InvoiceApplyHeader invoiceApplyHeader1 = invoiceApplyHeaderRepository.selectByPrimary(invoiceApplyHeader.getApplyHeaderId());
            invoiceApplyHeader.setObjectVersionNumber(invoiceApplyHeader1.getObjectVersionNumber());
            invoiceApplyHeader.setDelFlag(0);

            InvoiceApplyHeaderDTO invoiceApplyHeaderDTO = updateListDTO.get(i);

            List<InvoiceApplyLine> invoiceApplyLineList = invoiceApplyHeaderDTO.getInvoiceApplyLines();
            for(int k = 0; k < invoiceApplyLineList.size(); k++) {
                InvoiceApplyLine invoiceApplyLine = invoiceApplyLineList.get(k);
//                validate first
                InvoiceApplyLine invoiceApplyLineNew = new InvoiceApplyLine();
                invoiceApplyLineNew.setApplyHeaderId(invoiceApplyHeaderDTO.getApplyHeaderId());
//                code to set if apply line is null then set apply line to 0
//                the purpose to set it to 0 rather than null is so it can do query when i do select
                if(invoiceApplyLine.getApplyLineId() == null) {
                    invoiceApplyLineNew.setApplyLineId(0L);
                } else {
                    invoiceApplyLineNew.setApplyLineId(invoiceApplyLine.getApplyLineId());
                }

//                this is the selected query so when is 0 it will be setted as an insert because the size is 0
                List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineRepository.select(invoiceApplyLineNew);
                BigDecimal taxAmount = BigDecimal.ZERO;
                BigDecimal totalAmount = BigDecimal.ZERO;
                BigDecimal excludeTaxAmount = BigDecimal.ZERO;

                totalAmount = invoiceApplyLine.getUnitPrice().multiply(invoiceApplyLine.getQuantity());
                taxAmount = totalAmount.multiply(invoiceApplyLine.getTaxRate());
                excludeTaxAmount = totalAmount.subtract(taxAmount);

//                if size > 0 which apply line is exist, it will do update, else will insert
                if(invoiceApplyLines.size() > 0) {
                    InvoiceApplyLine invoiceApplyLine1 = invoiceApplyLines.get(0);
                    invoiceApplyLine.setObjectVersionNumber(invoiceApplyLine1.getObjectVersionNumber());

                    invoiceApplyLine.setApplyHeaderId(invoiceApplyHeaderDTO.getApplyHeaderId());

                    invoiceApplyLine.setTotalAmount(totalAmount);
                    invoiceApplyLine.setTaxAmount(taxAmount);
                    invoiceApplyLine.setExcludeTaxAmount(excludeTaxAmount);

                    invoiceApplyLineUpdate.add(invoiceApplyLine);
                } else {
                    invoiceApplyLine.setApplyHeaderId(invoiceApplyHeader.getApplyHeaderId());
                    invoiceApplyLine.setTotalAmount(totalAmount);
                    invoiceApplyLine.setTaxAmount(taxAmount);
                    invoiceApplyLine.setExcludeTaxAmount(excludeTaxAmount);

                    invoiceApplyLineInsert.add(invoiceApplyLine);
                }
            }

// update header also with calculated amount
            countApplyLineUpdateWithHeader(invoiceApplyHeader);
        }

        invoiceApplyLineRepository.batchInsertSelective(invoiceApplyLineInsert);
        invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(invoiceApplyLineUpdate);
    }

    public InvoiceApplyHeaderDTO detail(Long headerId) {
        String result = redisHelper.strGet("hpfm:demo:invoice" + headerId.toString());
//        if (result != null && !result.isEmpty()) {
//            return JSON.parseObject(result, InvoiceApplyHeaderDTO.class);
//        }

        InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(headerId);
        InvoiceApplyHeaderDTO invoiceApplyHeaderDTO = new InvoiceApplyHeaderDTO();

        InvoiceApplyLine invoiceApplyLine = new InvoiceApplyLine();
        invoiceApplyLine.setApplyHeaderId(headerId);

        List<InvoiceApplyLine> invoiceApplyLineList = invoiceApplyLineRepository.select(invoiceApplyLine);
        invoiceApplyHeaderDTO.setInvoiceApplyLines(invoiceApplyLineList);

        BeanUtils.copyProperties(invoiceApplyHeader, invoiceApplyHeaderDTO);

        CustomUserDetails user = DetailsHelper.getUserDetails();

//        User user = userRepository.selectByPrimary(invoiceApplyHeader.getCreatedBy());
        invoiceApplyHeaderDTO.setCreatedName(user.getRealName());

        String serializeDTO = JSON.toJSONString(invoiceApplyHeaderDTO);
        redisHelper.strSet("hpfm:demo:invoice" + headerId.toString(), serializeDTO);

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

//    this function count apply line and update the header using the parameter invoiceApplyHeader with header_id
    public void countApplyLineUpdateWithHeader (InvoiceApplyHeader invoiceApplyHeader) {
        InvoiceApplyLine invoiceApplyLineNew = new InvoiceApplyLine();
        invoiceApplyLineNew.setApplyHeaderId(invoiceApplyHeader.getApplyHeaderId());

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

        InvoiceApplyHeader invoiceApplyHeaders = invoiceApplyHeaderRepository.selectByPrimary(invoiceApplyHeader.getApplyHeaderId());
        invoiceApplyHeader.setTaxAmount(headerTaxAmount);
        invoiceApplyHeader.setExcludeTaxAmount(headerExcludeTaxAmount);
        invoiceApplyHeader.setTotalAmount(headerTotalAmount);
        invoiceApplyHeader.setObjectVersionNumber(invoiceApplyHeaders.getObjectVersionNumber());

        invoiceApplyHeaderRepository.updateByPrimaryKeySelective(invoiceApplyHeader);
    }

//    this function only to update header based on counted applyline using header_id only
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