package com.hand.demo.app.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.dto.InvoiceApplyReportQueryDTO;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.CodeRuleConstant;
import com.hand.demo.infra.constant.InvoiceApplyHeaderConstant;
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
import org.hzero.mybatis.domian.Condition;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author azhar.naufal@hand-global.com
 * @since 2024-11-04 10:11:56
 */
@Service
public class InvoiceApplyHeaderServiceImpl implements InvoiceApplyHeaderService {
    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;
    @Autowired
    private InvoiceApplyLineService lineService;
    @Autowired
    private LovAdapter lovAdapter;
    @Autowired
    private CodeRuleBuilder codeRuleBuilder;
    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private IamRemoteService iamRemoteService;

    @Override
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeaderDTO invoiceApplyHeaderDTO) {
        String iamUserString = iamRemoteService.selectSelf().getBody();
        JSONObject jsonIam = new JSONObject(iamUserString);

        Boolean tenantAdminFlag = jsonIam.optBoolean("tenantAdminFlag", false);

        if (invoiceApplyHeaderDTO.getDelFlag() == null) {
            invoiceApplyHeaderDTO.setDelFlag(0);
        }
        invoiceApplyHeaderDTO.setTenantAdminFlag(tenantAdminFlag);

        return PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyHeaderRepository.selectList(invoiceApplyHeaderDTO));
    }

    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @Override
    public Page<InvoiceApplyHeaderDTO> selectListExport(PageRequest pageRequest, InvoiceApplyHeaderDTO invoiceApplyHeaderDTO) {
        //Check DelFlag
        if (invoiceApplyHeaderDTO.getDelFlag() == null) {
            invoiceApplyHeaderDTO.setDelFlag(0);
        }

        //Create Page of Header, and Set Lines for Header
        Page<InvoiceApplyHeaderDTO> headers = PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyHeaderRepository.selectList(invoiceApplyHeaderDTO));

        //Collect All HeaderId, to query linelist
        List<Long> headerIds = new ArrayList<>();
        for (InvoiceApplyHeaderDTO dto : headers.getContent()) {
            headerIds.add(dto.getApplyHeaderId());
        }

        Condition condition = new Condition(InvoiceApplyLine.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andIn("applyHeaderId", headerIds);

        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineRepository.selectByCondition(condition);

        //Create Map for each header lines
        Map<Long, List<InvoiceApplyLine>> lineHeaders = invoiceApplyLines.stream()
                .collect(Collectors.groupingBy(InvoiceApplyLine::getApplyHeaderId));

        for (InvoiceApplyHeaderDTO headerDTO : headers.getContent()) {
            headerDTO.setInvoiceApplyLineList(lineHeaders.get(headerDTO.getApplyHeaderId()));
        }

        return headers;
    }

    @Override
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public InvoiceApplyHeaderDTO detail(Long headerId) {
        String cacheKey = "hpfm:demo:invoice:INVOICE_HEADER_" + headerId;

        //For Create Redis if the key is not in redis DB
        if (redisHelper.strGet(cacheKey) == null) {
            InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectByPrimaryKey(headerId);
            if (header == null || header.getDelFlag() == 1) {
                Object[] objects = new Object[]{headerId.toString()};
//                String errorMessage = MessageAccessor.getMessage(InvoiceApplyHeaderConstant.ERROR_NOT_FOUND, objects, Locale.US).getDesc();
                throw new CommonException(InvoiceApplyHeaderConstant.ERROR_NOT_FOUND, objects);

            }

            //Convert Entity to DTO
            InvoiceApplyHeaderDTO headerDTO = new InvoiceApplyHeaderDTO();
            List<InvoiceApplyLine> lineList = lineService.linesByHeaderId(headerId);
            BeanUtils.copyProperties(header, headerDTO);
            headerDTO.setInvoiceApplyLineList(lineList);

            //Create JSON and push/set to redis
            String jsonHeader = JSON.toJSONString(headerDTO);
            redisHelper.strSet(cacheKey, jsonHeader);
            return headerDTO;
        }

        return JSON.parseObject(redisHelper.strGet(cacheKey), InvoiceApplyHeaderDTO.class);
    }

    @Override
    public InvoiceApplyHeader getHeaderById(Long headerId) {
        return invoiceApplyHeaderRepository.selectByPrimary(headerId);
    }

    @Override
    @Transactional
    public void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOS) {
        //Return ListValidate from value set
        List<String> validApplyStatus = getValidLovValues(InvoiceApplyHeaderConstant.LovCode.APPLY_STATUS);
        List<String> validInvoiceType = getValidLovValues(InvoiceApplyHeaderConstant.LovCode.INVOICE_TYPE);
        List<String> validInvoiceColor = getValidLovValues(InvoiceApplyHeaderConstant.LovCode.INVOICE_COLOR);

        List<String> listError = new LinkedList<>();

        int lineNumber = 1;
        for (InvoiceApplyHeaderDTO invoiceDTO : invoiceApplyHeaderDTOS) {
            //Validation value Set
            validateInvoiceDTO(lineNumber, invoiceDTO, validApplyStatus, validInvoiceType, validInvoiceColor);
            if (invoiceDTO.getErrorMsg() != null) {
                listError.add(invoiceDTO.getErrorMsg());
            }
            lineNumber++;
        }

        // Throw Error if not Valid
        if (CollUtil.isNotEmpty(listError)) {
            String errors = String.join(" | ", listError);
            throw new CommonException(InvoiceApplyHeaderConstant.ERROR_GENERAL_MESSAGE, errors);
        }


        List<InvoiceApplyHeaderDTO> insertList = new LinkedList<>();
        List<InvoiceApplyHeaderDTO> updateList = new LinkedList<>();

        //insert or update
        insertOrUpdateHeader(invoiceApplyHeaderDTOS, insertList, updateList);

        //Save lines
        saveLines(insertList);
        saveLines(updateList);

    }


    private List<String> getValidLovValues(String lovCode) {
        return lovAdapter
                .queryLovValue(lovCode, BaseConstants.DEFAULT_TENANT_ID)
                .stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());
    }

    private void validateInvoiceDTO(int lineNumber, InvoiceApplyHeaderDTO invoiceDTO, List<String> validApplyStatus,
                                    List<String> validInvoiceType, List<String> validInvoiceColor) {
        if (!validApplyStatus.contains(invoiceDTO.getApplyStatus())) {
            invoiceDTO.setErrorMsg("Line " + lineNumber + " Apply Status is Invalid");
        }
        if (!validInvoiceType.contains(invoiceDTO.getInvoiceType())) {
            invoiceDTO.setErrorMsg("Line " + lineNumber + " Invoice Type is Invalid");
        }
        if (!validInvoiceColor.contains(invoiceDTO.getInvoiceColor())) {
            invoiceDTO.setErrorMsg("Line " + lineNumber + " Invoice Color is Invalid");
        }
    }

    private void insertOrUpdateHeader(List<InvoiceApplyHeaderDTO> requestList,
                                      List<InvoiceApplyHeaderDTO> insertList,
                                      List<InvoiceApplyHeaderDTO> updateList) {
        for (InvoiceApplyHeaderDTO headerDTO : requestList) {
            String cacheKey = "hpfm:demo:invoice:INVOICE_HEADER_" + headerDTO.getApplyHeaderId();
            if (headerDTO.getApplyHeaderId() == null) {
                String headerNumberBuilder = codeRuleBuilder.generateCode(CodeRuleConstant.CODE_RULE_HEADER_NUMBER, null);
                headerDTO.setApplyHeaderNumber(headerNumberBuilder);
                insertList.add(headerDTO);
            } else {
                updateList.add(headerDTO);
                redisHelper.delKey(cacheKey);
            }
        }
        invoiceApplyHeaderRepository.batchInsert(new ArrayList<>(insertList));
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(new ArrayList<>(updateList));
    }

    private void saveLines(List<InvoiceApplyHeaderDTO> listHeaderDTOSaved) {
        for (InvoiceApplyHeaderDTO headerDTO : listHeaderDTOSaved) {
            List<InvoiceApplyLine> lines = headerDTO.getInvoiceApplyLineList();
            if (CollUtil.isEmpty(lines)) {
                continue;
            }
            for (InvoiceApplyLine line : lines) {
                //Set headerId from the header
                line.setApplyHeaderId(headerDTO.getApplyHeaderId());
            }
            lineService.saveData(lines);
        }
    }


    @Override
    public void softDelete(Long applyHeaderId) {
        InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(applyHeaderId);
        if (invoiceApplyHeader.getDelFlag() == 1) {
            String errorMessage = "Invoice Header with number " + invoiceApplyHeader.getApplyHeaderNumber() + " already deleted";
            throw new CommonException(InvoiceApplyHeaderConstant.ERROR_SAVE, errorMessage);
        }
        invoiceApplyHeader.setDelFlag(1);
        invoiceApplyHeaderRepository.updateByPrimaryKeySelective(invoiceApplyHeader);
    }

    @Override
    public void batchSoftDelete(List<InvoiceApplyHeaderDTO> applyHeaderDTOS) {
        //Collect headerId to get the header that want to delete
        List<Long> headerIdsLong = new LinkedList<>();
        for (InvoiceApplyHeaderDTO headerDTO : applyHeaderDTOS) {
            headerIdsLong.add(headerDTO.getApplyHeaderId());
        }

        //Create String ids
        String headerIds = headerIdsLong.stream()
                .map(String::valueOf)  // Convert Long to String
                .collect(Collectors.joining(","));

        //Search the listOfHeader
        List<InvoiceApplyHeader> headersToDelete = invoiceApplyHeaderRepository.selectByIds(headerIds);

        //Checking if there deleted Header
        List<String> errorList = new LinkedList<>();
        for (InvoiceApplyHeader header : headersToDelete) {
            if (header.getDelFlag() == 1) {
                String errorMessage = "Invoice Header with number " + header.getApplyHeaderNumber() + " already deleted";
                errorList.add(errorMessage);
            } else {
                header.setDelFlag(1);
            }
        }

        //Throw Error if any deleted Header
        if (CollUtil.isNotEmpty(errorList)) {
            Object[] objects = new Object[]{String.join(" | ", errorList)};
            throw new CommonException(InvoiceApplyHeaderConstant.ERROR_SAVE, objects);
        }

        //UpdateDeleted
        invoiceApplyHeaderRepository.batchUpdateOptional(headersToDelete, InvoiceApplyHeader.FIELD_DEL_FLAG);
    }


    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @Override
    public List<InvoiceApplyHeaderDTO> selectListForDataSet(InvoiceApplyHeaderDTO invoiceApplyHeaderDTO) {
        List<InvoiceApplyHeaderDTO> headerDTOS = invoiceApplyHeaderRepository.selectListDataSet(invoiceApplyHeaderDTO);

        //Collect All HeaderId, to query linelist
        Set<Long> headerIds = new HashSet<>();
        for (InvoiceApplyHeaderDTO dto : headerDTOS) {
            headerIds.add(dto.getApplyHeaderId());
        }

        Condition condition = new Condition(InvoiceApplyLine.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andIn("applyHeaderId", headerIds);

        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineRepository.selectByCondition(condition);

        //Create Map for each header lines
        Map<Long, List<InvoiceApplyLine>> lineHeaders = invoiceApplyLines.stream()
                .collect(Collectors.groupingBy(InvoiceApplyLine::getApplyHeaderId));

        for (InvoiceApplyHeaderDTO headerDTO : headerDTOS) {
            headerDTO.setInvoiceApplyLineList(lineHeaders.get(headerDTO.getApplyHeaderId()));
            headerDTO.setSignName(DetailsHelper.getUserDetails().getRealName());
        }


        if (invoiceApplyHeaderDTO.getDelFlag() == null) {
            invoiceApplyHeaderDTO.setDelFlag(0);
        }

        return headerDTOS;
    }

//    @ProcessLovValue(targetField = "body.listHeader")
    @Override
    public InvoiceApplyReportQueryDTO selectListForExcel(InvoiceApplyHeaderDTO invoiceApplyHeaderDTO, Long organizationId) {
        List<String> listStatus = invoiceApplyHeaderDTO.getListApplyStatus();
        List<Map<String, Object>> listApplyStatusValue = lovAdapter.queryLovData(InvoiceApplyHeaderConstant.LovCode.APPLY_STATUS, organizationId, null, null, null, null);
        List<Map<String, Object>> listInvoiceType = lovAdapter.queryLovData(InvoiceApplyHeaderConstant.LovCode.INVOICE_TYPE, organizationId, null, null, null, null);

        //Get lov Value code
        ArrayList<String> listStatusBefore = new ArrayList<>();
        if(invoiceApplyHeaderDTO.getListApplyStatus() != null){
            listStatusBefore = new ArrayList<>(invoiceApplyHeaderDTO.getListApplyStatus());

            for (int i = 0; i < listStatus.size(); i++) {
                for (Map<String, Object> lovValue : listApplyStatusValue) {
                    if (listStatus.get(i).equals(lovValue.get("meaning"))) {
                        listStatus.set(i, (String) lovValue.get("value"));  // Set value to the list
                        break;
                    }
                }
            }
            invoiceApplyHeaderDTO.setListApplyStatus(listStatus);
        }

        String invoiceTypeBefore = "";
        if(invoiceApplyHeaderDTO.getInvoiceTypeParam() != null){
            String invoiceTypeCode = invoiceApplyHeaderDTO.getInvoiceTypeParam();
            invoiceTypeBefore = invoiceApplyHeaderDTO.getInvoiceTypeParam();
            for (Map<String, Object> lovValue : listInvoiceType){
                if(invoiceTypeCode.equals(lovValue.get("meaning"))){
                    invoiceTypeCode = (String) lovValue.get("value");
                    break;
                }
            }
            invoiceApplyHeaderDTO.setInvoiceTypeParam(invoiceTypeCode);
        }

        List<InvoiceApplyHeaderDTO> headersDTO = invoiceApplyHeaderRepository.selectList(invoiceApplyHeaderDTO);

        String iamUserString = iamRemoteService.selectSelf().getBody();
        JSONObject jsonIam = new JSONObject(iamUserString);
        String tenantName = jsonIam.getString("tenantName");

        //Collect All HeaderId, to query linelist
        Set<Long> headerIds = new HashSet<>();
        for (InvoiceApplyHeaderDTO header : headersDTO) {
            headerIds.add(header.getApplyHeaderId());
        }

        //Condition for query all lines for each saveHeader
        Condition conditionLineByHeader = new Condition(InvoiceApplyLine.class);
        Condition.Criteria criteriaLineByHeader = conditionLineByHeader.createCriteria();
        criteriaLineByHeader.andIn("applyHeaderId", headerIds);

        List<InvoiceApplyLine> linesByHeader = invoiceApplyLineRepository.selectByCondition(conditionLineByHeader);

        //Mapping lines for each header
        Map<Long, List<InvoiceApplyLine>> linesGroupedByHeaderId = linesByHeader.stream()
                .collect(Collectors.groupingBy(InvoiceApplyLine::getApplyHeaderId));

        for(InvoiceApplyHeaderDTO dto : headersDTO){
            List<InvoiceApplyLine> lines = (linesGroupedByHeaderId.get(dto.getApplyHeaderId()));
            if(lines != null){
                String concatInvoiceName = lines.stream().map(InvoiceApplyLine::getInvoiceName).collect(Collectors.joining(", "));
                dto.setListLineName(concatInvoiceName);
            }
        }

        InvoiceApplyReportQueryDTO reportQueryDTO = new InvoiceApplyReportQueryDTO();
        reportQueryDTO.setTenantName(tenantName);
        reportQueryDTO.setInvoiceNumberFrom(invoiceApplyHeaderDTO.getInvoiceNumberFrom());
        reportQueryDTO.setInvoiceNumberTo(invoiceApplyHeaderDTO.getInvoiceNumberTo());
        reportQueryDTO.setCreationDateFrom(invoiceApplyHeaderDTO.getCreationDateFrom());
        reportQueryDTO.setCreationDateTo(invoiceApplyHeaderDTO.getCreationDateTo());
        reportQueryDTO.setSubmitTimeFrom(invoiceApplyHeaderDTO.getSubmitTimeFrom());
        reportQueryDTO.setSubmitTimeTo(invoiceApplyHeaderDTO.getSubmitTimeTo());
        reportQueryDTO.setListApplyStatus(invoiceApplyHeaderDTO.getListApplyStatus());
        if(invoiceApplyHeaderDTO.getListApplyStatus() != null){
            reportQueryDTO.setListStatusString(listStatusBefore.stream().map(String::valueOf).collect(Collectors.joining(", ")));
        }
        reportQueryDTO.setInvoiceTypeParam(invoiceTypeBefore);
        reportQueryDTO.setListHeader(headersDTO);

        return reportQueryDTO;
    }
}

