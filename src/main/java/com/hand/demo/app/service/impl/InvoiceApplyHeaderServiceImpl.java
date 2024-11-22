package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.dto.InvoiceApplyInfoDTO;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.InvoiceApplyConstants;
import com.hand.demo.infra.constant.TaskConstants;
import com.hand.demo.infra.util.Utils;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import lombok.AllArgsConstructor;
import org.hzero.boot.apaas.common.userinfo.infra.feign.IamRemoteService;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.hzero.mybatis.domian.Condition;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    private IamRemoteService iamRemoteService;

    private final LovAdapter lovAdapter;

    private final CodeRuleBuilder codeRuleBuilder;

    private RedisHelper redisHelper;

    @Override
    public Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader) {

        JSONObject jsonObject = getIamRemoteObject();
        String tenantAdminFlag = "tenantAdminFlag";

        InvoiceApplyHeaderDTO invoiceApplyHeaderDTO = new InvoiceApplyHeaderDTO();
        BeanUtils.copyProperties(invoiceApplyHeader, invoiceApplyHeaderDTO);

        Boolean isAdmin = jsonObject.getBooleanValue(tenantAdminFlag);
        invoiceApplyHeaderDTO.setIsAdminFlag(isAdmin);

        if (invoiceApplyHeaderDTO.getDelFlag() == null) {
            invoiceApplyHeaderDTO.setDelFlag(0);
        }

        Page<InvoiceApplyHeader> headers = PageHelper.doPageAndSort(pageRequest, () ->
                invoiceApplyHeaderRepository.selectList(invoiceApplyHeaderDTO)
        );


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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOs) {
        valueSetValidation(invoiceApplyHeaderDTOs);

        List<InvoiceApplyHeaderDTO> insertList = invoiceApplyHeaderDTOs.stream()
                .filter(header -> header.getApplyHeaderId() == null)
                .collect(Collectors.toList());

        List<InvoiceApplyHeaderDTO> updateList = invoiceApplyHeaderDTOs.stream()
                .filter(header -> header.getApplyHeaderId() != null)
                .peek(header -> {
                    header.setTotalAmount(null);
                    header.setTaxAmount(null);
                    header.setExcludeTaxAmount(null);
                })
                .collect(Collectors.toList());

        Map<String, String> variableMap = new HashMap<>();
        variableMap.put("customSegment", "-");

        List<String> applyHeaderNumbers = codeRuleBuilder.generateCode(insertList.size(), TaskConstants.CODE_RULE, variableMap);

        for (int i = 0; i < insertList.size(); i++) {
            InvoiceApplyHeaderDTO headerDTO = insertList.get(i);
            headerDTO.setApplyHeaderNumber(applyHeaderNumbers.get(i));
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

        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(new ArrayList<>(updateList));

        if(!invoiceApplyLines.isEmpty()) {
            invoiceApplyLineService.saveData(invoiceApplyLines);
        }

        redisHelper.delKey(InvoiceApplyConstants.REDIS_KEY);
    }

    @Override
    public void softDeleteById(Long applyHeaderId) {
        InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(applyHeaderId);
        invoiceApplyHeader.setDelFlag(1);
        invoiceApplyHeaderRepository.updateOptional(invoiceApplyHeader, InvoiceApplyHeader.FIELD_DEL_FLAG);
        redisHelper.delKey(InvoiceApplyConstants.REDIS_KEY);
    }

    @Override
    public InvoiceApplyHeaderDTO detail(Long applyHeaderId) {
        String key = InvoiceApplyConstants.REDIS_KEY + applyHeaderId;

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

        String sign = DetailsHelper.getUserDetails().getRealName();
        invoiceApplyHeaderDTO.setSign(sign);

        String headerJson = JSON.toJSONString(invoiceApplyHeaderDTO);
        redisHelper.strSet(key, headerJson);
        System.out.println("alsjdnqwed: no reddiss");

        return invoiceApplyHeaderDTO;
    }

    @Override
    public InvoiceApplyInfoDTO getInvoiceApplyInfo(InvoiceApplyInfoDTO infoDTO) {
        JSONObject jsonObject = getIamRemoteObject();
        String tenantName = jsonObject.getString("tenantName");
        infoDTO.setTenantName(tenantName);

        List<String> applyStatuses = infoDTO.getApplyStatus();
        if (applyStatuses != null && !applyStatuses.isEmpty()) {
            List<String> newApplyStatuses = getApplyStatusCondition(applyStatuses);
            if (newApplyStatuses != null && !newApplyStatuses.isEmpty()) {
                infoDTO.setApplyStatus(newApplyStatuses);
            }
        }

        String invoiceType = infoDTO.getInvoiceType();
        if (invoiceType != null && !invoiceType.isEmpty()) {
            String newInvoiceType = getInvoiceTypeCondition(invoiceType);
            infoDTO.setInvoiceType(newInvoiceType);
        }

        List<InvoiceApplyHeaderDTO> headerDTOS = invoiceApplyHeaderRepository.selectHeaderInfo(infoDTO);

        List<InvoiceApplyHeaderDTO> headerDTOs = headerDTOS.stream()
                .map(header -> {
                    InvoiceApplyHeaderDTO headerDTO = new InvoiceApplyHeaderDTO();
                    BeanUtils.copyProperties(header, headerDTO);
                    return headerDTO;
                })
                .collect(Collectors.toList());

        infoDTO.setInvoiceApplyHeaderList(headerDTOs);
        infoDTO.setApplyStatus(applyStatuses);
        infoDTO.setInvoiceType(invoiceType);

        return infoDTO;
    }
//    @Override
//    public InvoiceApplyInfoDTO getInvoiceApplyInfo(InvoiceApplyInfoDTO infoDTO) {
//        JSONObject jsonObject = getIamRemoteObject();
//        Long tenantId = jsonObject.getLong("tenantId");
//        String tenantName = jsonObject.getString("tenantName");
//        Condition condition = new Condition(InvoiceApplyHeader.class);
//        Condition.Criteria criteria = condition.createCriteria();
//        criteria.andEqualTo(InvoiceApplyHeader.FIELD_TENANT_ID, tenantId);
//
//        setDateCondition(criteria, InvoiceApplyHeader.FIELD_CREATION_DATE, infoDTO.getInvoiceCreationDateFrom(), infoDTO.getInvoiceCreationDateTo());
//        setDateCondition(criteria, InvoiceApplyHeader.FIELD_SUBMIT_TIME, infoDTO.getSubmitTimeFrom(), infoDTO.getSubmitTimeTo());
//        setApplyNumberCondition(criteria, InvoiceApplyHeader.FIELD_APPLY_HEADER_NUMBER, infoDTO.getInvoiceApplyNumberFrom(), infoDTO.getInvoiceApplyNumberTo());
//        setApplyStatusCondition(criteria, infoDTO.getApplyStatus());
//        setInvoiceTypeCondition(criteria, infoDTO.getInvoiceType());
//
//        List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceApplyHeaderRepository.selectByCondition(condition);
//
//        List<InvoiceApplyHeaderDTO> headerDTOs = invoiceApplyHeaders.stream()
//                .map(header -> {
//                    InvoiceApplyHeaderDTO headerDTO = new InvoiceApplyHeaderDTO();
//                    BeanUtils.copyProperties(header, headerDTO);
//                    return headerDTO;
//                })
//                .collect(Collectors.toList());
//        infoDTO.setTenantName(tenantName);
//        infoDTO.setInvoiceApplyHeaderList(headerDTOs);
//
//        Set<Long> applyHeaderIds = headerDTOs.stream().map(InvoiceApplyHeader::getApplyHeaderId).collect(Collectors.toSet());
//        Condition applyLineCondition = new Condition(InvoiceApplyLine.class);
//        Condition.Criteria applyLineCriteria = applyLineCondition.createCriteria();
//        applyLineCriteria.andIn(InvoiceApplyLine.FIELD_APPLY_HEADER_ID, applyHeaderIds);
//
//        if (!applyHeaderIds.isEmpty()) {
//            List<InvoiceApplyLine> applyLines = invoiceApplyLineRepository.selectByCondition(applyLineCondition);
//            Map<Long, List<InvoiceApplyLine>> lineByHeaderIds = applyLines.stream().collect(Collectors.groupingBy(InvoiceApplyLine::getApplyHeaderId));
//            for(InvoiceApplyHeaderDTO applyHeaderDTO : headerDTOs) {
//                List<InvoiceApplyLine> lines = lineByHeaderIds.get(applyHeaderDTO.getApplyHeaderId());
//                if (lines != null && !lines.isEmpty()) {
//                    String invoiceNames = lines.stream().map(InvoiceApplyLine::getInvoiceName).collect(Collectors.joining(", "));
//                    applyHeaderDTO.setInvoiceName(invoiceNames);
//                }
//            }
//        }
//
//        return infoDTO;
//    }

    private Date convertToDate(LocalDateTime date) {
        return Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
    }

//    private void setDateCondition(Condition.Criteria criteria, String fieldName, LocalDate from, LocalDate to) {
//        if (from != null && to != null) {
//            LocalDateTime newFrom = from.atStartOfDay();
//            LocalDateTime newTo = to.atTime(LocalTime.MAX);
//            criteria.andBetween(fieldName, convertToDate(newFrom), convertToDate(newTo));
//        } else if (from != null) {
//            LocalDateTime newFrom = from.atStartOfDay();
//            criteria.andGreaterThanOrEqualTo(fieldName, convertToDate(newFrom));
//        } else if (to != null) {
//            LocalDateTime newTo = to.atTime(LocalTime.MAX);
//            criteria.andLessThanOrEqualTo(fieldName, convertToDate(newTo));
//        }
//    }
//
//    private void setApplyNumberCondition(Condition.Criteria criteria, String fieldName, String from, String to) {
//        if (from != null && to != null) {
//            criteria.andBetween(fieldName, from, to);
//        } else if (from != null) {
//            criteria.andGreaterThanOrEqualTo(fieldName, from);
//        } else if (to != null) {
//            criteria.andLessThanOrEqualTo(fieldName, to);
//        }
//    }

    private List<String> getApplyStatusCondition(List<String> applyStatus) {
        if (applyStatus != null && !applyStatus.isEmpty()) {
            List<LovValueDTO> lovList = lovAdapter.queryLovValue(InvoiceApplyConstants.INV_APPLY_HEADER_APPLY_STATUS, BaseConstants.DEFAULT_TENANT_ID);
            return lovList.stream().filter(lov -> applyStatus.contains(lov.getMeaning())).map(LovValueDTO::getValue).collect(Collectors.toList());
        }
        return null;
    }

//    private void setApplyStatusCondition(Condition.Criteria criteria, List<String> applyStatus) {
//        if (applyStatus != null && !applyStatus.isEmpty()) {
////            valueSetMeaningValidation(applyStatus);
//            List<LovValueDTO> lovList = lovAdapter.queryLovValue(InvoiceApplyConstants.INV_APPLY_HEADER_APPLY_STATUS, BaseConstants.DEFAULT_TENANT_ID);
//
//            Set<String> valueSets = new HashSet<>();
//
//            for (String statusMeaning : applyStatus) {
//                for (LovValueDTO lov : lovList) {
//                    if (statusMeaning.equals(lov.getMeaning())) {
//                        valueSets.add(lov.getValue());
//                        break;
//                    }
//                }
//            }
//
//            if (!valueSets.isEmpty()) {
//                criteria.andIn(InvoiceApplyHeader.FIELD_APPLY_STATUS, valueSets);
//            }
//        }
//    }

    private String getInvoiceTypeCondition(String invoiceType) {
        if(invoiceType != null) {
            List<LovValueDTO> lovList = lovAdapter.queryLovValue(InvoiceApplyConstants.INV_APPLY_HEADER_INV_TYPE, BaseConstants.DEFAULT_TENANT_ID);

            for (LovValueDTO lov : lovList) {
                if(invoiceType.equals(lov.getMeaning())) {
                    return lov.getValue();
                }
            }
        }
        return null;
    }

//    private void setInvoiceTypeCondition(Condition.Criteria criteria, String invoiceType) {
//        if(invoiceType != null) {
//            List<LovValueDTO> lovList = lovAdapter.queryLovValue(InvoiceApplyConstants.INV_APPLY_HEADER_INV_TYPE, BaseConstants.DEFAULT_TENANT_ID);
//
//            for (LovValueDTO lov : lovList) {
//                if(invoiceType.equals(lov.getMeaning())) {
//                    criteria.andEqualTo(InvoiceApplyHeader.FIELD_INVOICE_TYPE, lov.getValue());
//                    break;
//                }
//            }
//        }
//    }


    private void valueSetValidation(List<InvoiceApplyHeaderDTO> dtos) {
        List<String> errors = new ArrayList<>();

        dtos.forEach(dto -> {
            String applyStatus = dto.getApplyStatus();
            String invoiceType = dto.getInvoiceType();
            String invoiceColor = dto.getInvoiceColor();

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
                if(applyStatus != null) {
                    errors.add(applyStatus + " is not a valid apply status");
                }
            }

            if (!allowedInvoiceType.contains(invoiceType)) {
                if(invoiceType != null) {
                    errors.add(invoiceType + " is not a valid invoice type");
                    }
            }

            if(!allowedInvoiceColor.contains(invoiceColor)) {
                if(invoiceColor != null) {
                    errors.add(invoiceColor + " is not a valid invoice color");
                }
            }
        });

        if (!errors.isEmpty()) {
            throw new CommonException(InvoiceApplyConstants.INV_APPLY_HEADER_ERROR, errors.toString());
        }
    }

//    private void valueSetMeaningValidation(List<String> meanings) {
//        List<String> errors = new ArrayList<>();
//        List<String> allowedApplyStatuses = lovAdapter.queryLovValue(InvoiceApplyConstants.INV_APPLY_HEADER_APPLY_STATUS, BaseConstants.DEFAULT_TENANT_ID)
//                .stream()
//                .map(LovValueDTO::getMeaning)
//                .collect(Collectors.toList());
//
//        meanings.forEach(meaning -> {
//            if (!allowedApplyStatuses.contains(meaning)) {
//                errors.add(meaning + " is not a valid apply status");
//            }
//        });
//
//        if (!errors.isEmpty()) {
//            throw new CommonException(InvoiceApplyConstants.INV_APPLY_HEADER_ERROR, errors.toString());
//        }
//    }

    private JSONObject getIamRemoteObject() {
        return JSON.parseObject(iamRemoteService.selectSelf().getBody());
    }
}

