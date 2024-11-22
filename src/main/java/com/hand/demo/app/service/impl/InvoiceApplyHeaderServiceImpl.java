package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.api.dto.InfoHeaderDTO;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.dto.InvoiceHeaderReportDTO;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.*;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.InvHeaderConstant;
import com.hand.demo.infra.constant.TaskConstant;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.apaas.common.userinfo.infra.feign.IamRemoteService;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.json.JSONObject;
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

    @Autowired
    IamRemoteService iamRemoteService;

    @Autowired
    InvoiceApplyLineService invoiceApplyLineService;

    // Get list of header and lines
    @Override
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeaderDTO invoiceApplyHeader) {
        String remoteResponse = iamRemoteService.selectSelf().getBody();
        JSONObject jsonObject = new JSONObject(remoteResponse);
        Boolean adminFlag = false;
        if (jsonObject.has("superTenantAdminFlag")) {
            adminFlag = jsonObject.getBoolean("superTenantAdminFlag");
        } else if(jsonObject.has("tenantAdminFlag")){
            adminFlag = jsonObject.getBoolean("tenantAdminFlag");
        }
        invoiceApplyHeader.setSuperTenantAdminFlag(adminFlag);

        Page<InvoiceApplyHeader> pageResult = PageHelper.doPageAndSort(pageRequest, () ->
                invoiceApplyHeaderRepository.selectList(invoiceApplyHeader));

//        if (!jsonObject.has("superTenantAdminFlag")) {
//            pageResult = PageHelper.doPageAndSort(pageRequest, () -> {
//                        InvoiceApplyHeader header = new InvoiceApplyHeader();
//                        header.setCreatedBy(userId);
//                        return invoiceApplyHeaderRepository.selectList(header);
//
//                    }
//            );
//        }

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

    // Save the header that may accept line as param also update
    @Override
    @Transactional(rollbackFor = Exception.class)
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders) {
//        validationOfHeader(invoiceApplyHeaders);

        Map<String, String> variableMap = new HashMap<>();
        Map<String, List<InvoiceApplyLine>> stringListMap = new HashMap<>();
        variableMap.put("customSegment", "-");

        List<InvoiceApplyLine> lines = new ArrayList<>();

        invoiceApplyHeaders.forEach(header -> {
            if (header.getApplyHeaderNumber() == null) {
                String headerNumber = codeRuleBuilder.generateCode(InvHeaderConstant.RULE_CODE, variableMap);
                header.setApplyHeaderNumber(headerNumber);
                stringListMap.put(headerNumber, header.getInvoiceApplyLines());
            } else {
                stringListMap.put(header.getApplyHeaderNumber(), header.getInvoiceApplyLines());
            }

            initializeHeaderAmounts(header);
        });

        List<InvoiceApplyHeader> insertList = invoiceApplyHeaders.stream()
                .filter(header -> header.getApplyHeaderId() == null)
                .collect(Collectors.toList());
        List<InvoiceApplyHeader> updateList = invoiceApplyHeaders.stream()
                .filter(header -> header.getApplyHeaderId() != null)
                .collect(Collectors.toList());

        List<InvoiceApplyHeader> insertResult = invoiceApplyHeaderRepository.batchInsertSelective(insertList);
        insertResult.forEach(header -> {
            List<InvoiceApplyLine> lineMap = stringListMap.get(header.getApplyHeaderNumber());
            // populate lines with header id
            for (InvoiceApplyLine line : lineMap) {
                line.setApplyHeaderId(header.getApplyHeaderId());
                lines.add(line);
            }
        });

        List<InvoiceApplyHeader> updateResult = invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateList);
        updateResult.forEach(header -> {
            List<InvoiceApplyLine> lineMap = stringListMap.get(header.getApplyHeaderNumber());
            lines.addAll(lineMap);
        });
        invoiceApplyLineService.saveData(lines);
    }

    private void initializeHeaderAmounts(InvoiceApplyHeader header) {
        header.setTotalAmount(BigDecimal.valueOf(0));
        header.setExcludeTaxAmount(BigDecimal.valueOf(0));
        header.setTaxAmount(BigDecimal.valueOf(0));
    }

    // soft delete header
    @Override
    public InvoiceApplyHeaderDTO delete(Long id) {

        InvoiceApplyHeader data = invoiceApplyHeaderRepository.selectByPrimaryKey(id);
        if (data == null) {
            throw new CommonException("Data hasnt found");
        }
        InvoiceApplyHeaderDTO dto;
        data.setDelFlag(1);
        dto = changeToDTO(data);
        redisHelper.delKey(id + InvHeaderConstant.PREFIX);
        return dto;
    }

    @Override
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public InvoiceApplyHeaderDTO detail(Long id) {
        // check on redis if there's no value on redis it will set and return if the key is exist
        String remoteResponse = iamRemoteService.selectSelf().getBody();
        JSONObject jsonObject = new JSONObject(remoteResponse);
        if (Boolean.TRUE.equals(redisHelper.hasKey(String.valueOf(id)))) {
            String result = redisHelper.strGet(String.valueOf(id));
            if (!result.isEmpty()) {
                return JSON.parseObject(result, InvoiceApplyHeaderDTO.class);
            }
        }

        InvoiceApplyHeader getHeader = invoiceApplyHeaderRepository.selectByPrimaryKey(id);
        List<InvoiceApplyLine> listLines= invoiceApplyLineRepository.select("applyHeaderId", getHeader.getApplyHeaderId());
        InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();
        BeanUtils.copyProperties(getHeader, dto);
        dto.setInvoiceApplyLines(listLines);
        dto.setConcatInvoiceName(dto.getInvoiceApplyLines().stream().map(InvoiceApplyLine::getInvoiceName)
                .collect(Collectors.joining(", ")));
        dto.setTenantName(jsonObject.getString("tenantName"));
        dto.setUserIamName(DetailsHelper.getUserDetails().getRealName());

        String serializeDTO = JSON.toJSONString(dto);
        redisHelper.strSet(id + InvHeaderConstant.PREFIX, serializeDTO);

        return dto;
    }


    @Override
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public InvoiceHeaderReportDTO detailReportExcel(InvoiceApplyHeaderDTO invoiceApplyHeader, Long organizationId) {
        // Mapping logic directly in the method
        String remoteResponse = iamRemoteService.selectSelf().getBody();
        JSONObject jsonObject = new JSONObject(remoteResponse);
        InvoiceHeaderReportDTO invoiceHeadeReportDTO = new InvoiceHeaderReportDTO();
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        List<LovValueDTO> listInvoiceType = lovAdapter.queryLovValue(InvHeaderConstant.APPLY_TYPE_CODE, organizationId);
        List<LovValueDTO> listApplyStatus = lovAdapter.queryLovValue(InvHeaderConstant.APPLY_STATUS_CODE, organizationId);

        Map<String, String> invoiceType = listInvoiceType.stream().collect(Collectors.toMap(
                LovValueDTO::getMeaning, LovValueDTO::getValue
        ));

        Map<String, String> applyStatus = listApplyStatus.stream().collect(Collectors.toMap(
                LovValueDTO::getMeaning, LovValueDTO::getValue
        ));

        if (invoiceType.containsKey(invoiceApplyHeader.getInvoiceType())) {
            invoiceApplyHeader.setInvoiceType(invoiceType.get(invoiceApplyHeader.getInvoiceType()));
            invoiceHeadeReportDTO.setInvoiceTypeParam(invoiceApplyHeader.getInvoiceType());
        }

        if (invoiceApplyHeader.getListApplyStatus() != null) {
            List<String> transformedTypes = invoiceApplyHeader.getListApplyStatus().stream()
                    .map(type -> applyStatus.getOrDefault(type, type)) // Use the map to transform
                    .collect(Collectors.toList());
            invoiceHeadeReportDTO.setApplyStatusParam(invoiceApplyHeader.getListApplyStatus().stream()
                    .collect(Collectors.joining(",")));
            invoiceApplyHeader.setListApplyStatus(transformedTypes); // Update the DTO with the transformed list
        }

        List<InvoiceApplyHeaderDTO> dtos = invoiceApplyHeaderRepository.selectList(invoiceApplyHeader);
        for (InvoiceApplyHeaderDTO dto : dtos) {
          List<InvoiceApplyLine> listLine = invoiceApplyLineRepository.select(InvoiceApplyLine.FIELD_APPLY_HEADER_ID,
                  dto.getApplyHeaderId());
//          dto.setUserIamName(DetailsHelper.getUserDetails().getRealName());
//          dto.setSubmitTimeString(formatter.format(dto.getSubmitTime()));
          String concatListName = listLine.stream().map(InvoiceApplyLine::getInvoiceName)
                  .collect(Collectors.joining(", "));
            dto.setConcatInvoiceName(concatListName);
//          dto.setTenantName(jsonObject.getString("tenantName"));
        }

        invoiceHeadeReportDTO.setListHeader(dtos);

        invoiceHeadeReportDTO.setInvoiceApplyNumberFrom(dtos.stream()
                .min(Comparator.comparing(InvoiceApplyHeaderDTO::getApplyHeaderNumber))
                .map(InvoiceApplyHeaderDTO::getApplyHeaderNumber).orElse(null));
        invoiceHeadeReportDTO.setInvoiceApplyNumberTo(dtos.stream()
                .max(Comparator.comparing(InvoiceApplyHeaderDTO::getApplyHeaderNumber))
                .map(InvoiceApplyHeaderDTO::getApplyHeaderNumber).orElse(null));
        invoiceHeadeReportDTO.setInvoiceCreationDateFrom(dtos.stream()
                .min(Comparator.comparing(InvoiceApplyHeaderDTO::getCreationDate))
                .map(InvoiceApplyHeaderDTO::getCreationDate)
//                .map(creationDate -> formatter.format(creationDate))
                .orElse(null));
        invoiceHeadeReportDTO.setInvoiceCreationDateTo(dtos.stream()
                .max(Comparator.comparing(InvoiceApplyHeaderDTO::getCreationDate))
                .map(InvoiceApplyHeaderDTO::getCreationDate)
//                .map(creationDate -> formatter.format(creationDate))
                .orElse(null));
        invoiceHeadeReportDTO.setInvoiceSubmitTimeFrom(dtos.stream()
                .min(Comparator.comparing(InvoiceApplyHeaderDTO::getCreationDate))
                .map(InvoiceApplyHeaderDTO::getSubmitTime)
//                .map(submitTime -> formatter.format(submitTime))
                .orElse(null));
        invoiceHeadeReportDTO.setInvoiceSubmitTimeTo(dtos.stream()
                .min(Comparator.comparing(InvoiceApplyHeaderDTO::getCreationDate))
                .map(InvoiceApplyHeaderDTO::getSubmitTime)
//                .map(submitTime -> formatter.format(submitTime))
                .orElse(null));
//        invoiceHeadeReportDTO.setApplyStatusParam(dtos.stream().map());

//        invoiceHeadeReportDTO.setApplyStatusParam(dtos.stream().map(InvoiceApplyHeaderDTO::getApplyStatus)
//                .collect(Collectors.joining(", ")));
        invoiceHeadeReportDTO.setTenantNameReport(jsonObject.getString("tenantName"));
        invoiceHeadeReportDTO.setUserIamNameReport(DetailsHelper.getUserDetails().getRealName());

        return invoiceHeadeReportDTO;
    }


    // Validation using Lov Adapter based on value set on HZERO
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

    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @Override
    public InfoHeaderDTO validationOfHeaderLowCode(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders) {
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
        InfoHeaderDTO infoHeaderDTO = new InfoHeaderDTO();
//        for (int i = 0; i < invoiceApplyHeaders.size(); i++) {
        for (InvoiceApplyHeaderDTO invoiceApplyHeader : invoiceApplyHeaders) {
            StringBuilder lineError = new StringBuilder();

            if (!validColorTypes.contains(invoiceApplyHeader.getInvoiceColor())) {
//                lineError.append("Invoice color: ").append(invoiceApplyHeader.getInvoiceColor()).append(", ");
                invoiceApplyHeader.setErrorMsg(String.valueOf(lineError.append("Invoice color: ")
                        .append(invoiceApplyHeader.getInvoiceColor()).append(", ")));
            }
            if (!validApplyTypes.contains(invoiceApplyHeader.getInvoiceType())) {
//                lineError.append("Invoice Type: ").append(invoiceApplyHeader.getInvoiceType()).append(", ");
                invoiceApplyHeader.setErrorMsg(String.valueOf(lineError.append("Invoice Type: ")
                        .append(invoiceApplyHeader.getInvoiceType()).append(", ")));
            }
            if (!validStatusTypes.contains(invoiceApplyHeader.getApplyStatus())) {
//                lineError.append("Apply status: ").append(invoiceApplyHeader.getApplyStatus()).append(", ");
                invoiceApplyHeader.setErrorMsg(String.valueOf( lineError.append("Apply status: ")
                        .append(invoiceApplyHeader.getApplyStatus()).append(", ")));
            }

            if (lineError.length() > 0) {
                errorMessages.append(lineError.append("\n"));
            }
        }

        infoHeaderDTO.setErrorMsgList(
                invoiceApplyHeaders.stream()
                        .filter(header -> header.getErrorMsg() != null)
                        .collect(Collectors.toList())
        );
        infoHeaderDTO.setErrorMsg(errorMessages.toString());
        if (errorMessages.length() == 0 ) {
            infoHeaderDTO.setSuccessMsg("Success");
        }

//        String finalErrorMessages = errorMessages.toString();
//        if (errorMessages.length() > 0) {
//            throw new CommonException(finalErrorMessages);
//        }

        return infoHeaderDTO;
    }

    private InvoiceApplyHeaderDTO changeToDTO(InvoiceApplyHeader invoiceApplyHeaderDTO) {
        InvoiceApplyHeaderDTO dto = new InvoiceApplyHeaderDTO();
        BeanUtils.copyProperties(invoiceApplyHeaderDTO, dto);
        return dto;
    }
}

