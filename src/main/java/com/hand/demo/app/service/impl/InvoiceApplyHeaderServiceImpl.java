package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.dto.InvoiceApplyLineDTO;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.Constants;
import com.hand.demo.infra.util.Utils;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.hzero.mybatis.domian.Condition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;

import javax.rmi.CORBA.Util;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author lareza.farhan@hand-global.com
 * @since 2024-11-04 10:14:20
 */
@Service
public class InvoiceApplyHeaderServiceImpl implements InvoiceApplyHeaderService {
    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;
    @Autowired
    private CodeRuleBuilder codeRuleBuilder;
    @Autowired
    private LovAdapter lovAdapter;

    @Autowired
    private RedisHelper redisHelper;

    @Override
    public Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader) {
        Page<InvoiceApplyHeader> page =  PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyHeaderRepository.selectList(invoiceApplyHeader));

        List<InvoiceApplyHeaderDTO> convertedHeaders = new ArrayList<>();
        for(InvoiceApplyHeader header: page.getContent()){
            InvoiceApplyHeaderDTO convertedHeader = new InvoiceApplyHeaderDTO();
            BeanUtils.copyProperties(header,convertedHeader);
            convertedHeaders.add(convertedHeader);
        }
        populateLines(convertedHeaders);

        Page<InvoiceApplyHeaderDTO> convertedPage = new Page<>();
        BeanUtils.copyProperties(page,convertedPage);
        convertedPage.setContent(convertedHeaders);

        return convertedPage;
    }

    @Override
    public InvoiceApplyHeaderDTO detail(Long id) {
        InvoiceApplyHeaderDTO invoiceApplyHeaderDTO = getHeaderByCache(id);
        if(invoiceApplyHeaderDTO == null) {
            InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(id);
            invoiceApplyHeaderDTO = new InvoiceApplyHeaderDTO();
            BeanUtils.copyProperties(invoiceApplyHeader, invoiceApplyHeaderDTO);
            populateLines(Collections.singletonList(invoiceApplyHeaderDTO));
            setHeaderByCache(invoiceApplyHeaderDTO);
        }

        return invoiceApplyHeaderDTO;
    }

    @Override
    public void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOS) {
        validate(invoiceApplyHeaderDTOS);
        List<InvoiceApplyHeaderDTO> insertHeaderDTOS = invoiceApplyHeaderDTOS.stream().filter(header -> header.getApplyHeaderId() == null).collect(Collectors.toList());
        List<InvoiceApplyHeaderDTO> updateHeaderDTOS = invoiceApplyHeaderDTOS.stream().filter(header -> header.getApplyHeaderId() != null).collect(Collectors.toList());
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyHeaderDTOS.stream().flatMap(header -> header.getInvoiceApplyLineList().stream()).collect(Collectors.toList());
        Utils.calcInvoiceLineAmounts(invoiceApplyLines);
        Utils.calcAddInvoiceHeaderAmounts(invoiceApplyHeaderDTOS);
        if(!insertHeaderDTOS.isEmpty()) {
            insertHeaders(insertHeaderDTOS);
        }
        if(!updateHeaderDTOS.isEmpty()){
            updateHeaders(updateHeaderDTOS);
            delHeaderCache(updateHeaderDTOS);
        }
        insertLines(invoiceApplyLines);
    }

    @Override
    public void delete(List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOS) {
        deleteHeaders(invoiceApplyHeaderDTOS);
        delHeaderCache(invoiceApplyHeaderDTOS);
    }

    private void validate(List<InvoiceApplyHeaderDTO>invoiceApplyHeaderDTOS){
        List<LovValueDTO> applyStatusLov = lovAdapter.queryLovValue(Constants.LOV_INV_APPLY_HEADER_APPLY_STATUS, BaseConstants.DEFAULT_TENANT_ID);
        List<LovValueDTO> invTypeLov = lovAdapter.queryLovValue(Constants.LOV_INV_APPLY_HEADER_INV_TYPE, BaseConstants.DEFAULT_TENANT_ID);
        List<LovValueDTO> invColorLov = lovAdapter.queryLovValue(Constants.LOV_INV_APPLY_HEADER_INV_COLOR, BaseConstants.DEFAULT_TENANT_ID);

        List<String> applyStatuses = applyStatusLov.stream().map(LovValueDTO::getValue).collect(Collectors.toList());
        List<String> invTypes = invTypeLov.stream().map(LovValueDTO::getValue).collect(Collectors.toList());
        List<String> invColors = invColorLov.stream().map(LovValueDTO::getValue).collect(Collectors.toList());

        List<Integer> badHeaderIndex = new ArrayList<>();
        for (int i = 0; i<invoiceApplyHeaderDTOS.size(); i++){
            InvoiceApplyHeaderDTO invoiceApplyHeaderDTO = invoiceApplyHeaderDTOS.get(i);
            if(!applyStatuses.contains(invoiceApplyHeaderDTO.getApplyStatus())
                || !invTypes.contains(invoiceApplyHeaderDTO.getInvoiceType())
                || !invColors.contains(invoiceApplyHeaderDTO.getInvoiceColor())){
                badHeaderIndex.add(i);
            }else if(invoiceApplyHeaderDTO.getTotalAmount()!=null
                || invoiceApplyHeaderDTO.getTaxAmount()!=null
                || invoiceApplyHeaderDTO.getExcludeTaxAmount()!=null){
                badHeaderIndex.add(i);
            } else if (invoiceApplyHeaderDTO.getDelFlag()!=null) {
                badHeaderIndex.add(i);
            }
        }

        if(!badHeaderIndex.isEmpty()){
            throw new CommonException(Constants.MULTILINGUAL_INV_APPLY_HEADER_SAVE_ERROR,"Bad request in the following item index: "+ badHeaderIndex);
        }

        List<InvoiceApplyHeaderDTO> updateHeaderDTOS = invoiceApplyHeaderDTOS.stream().filter(line -> line.getApplyHeaderId() != null).collect(Collectors.toList());
        if(!updateHeaderDTOS.isEmpty()){
            List<InvoiceApplyHeader> foundUpdateHeaders = invoiceApplyHeaderRepository.selectByIds(invoiceApplyHeaderDTOS.stream().map(header->header.getApplyHeaderId().toString()).collect(Collectors.joining(",")));
            if(updateHeaderDTOS.size() != foundUpdateHeaders.size()){
                throw new CommonException(Constants.MULTILINGUAL_INV_APPLY_HEADER_SAVE_ERROR,"provided Ids and found ids is diff");
            }
        }
    }

    private  void insertHeaders(List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOS){
        List<String> applyHeaderNumbers = codeRuleBuilder.generateCode(invoiceApplyHeaderDTOS.size(), Constants.CODERULE_INV_APPLY_HEADER,null);
        Map<String, InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOMap = new HashMap<>();
        for(int i=0;i<invoiceApplyHeaderDTOS.size();i++){
            InvoiceApplyHeaderDTO invoiceApplyHeaderDTO =invoiceApplyHeaderDTOS.get(i);
            invoiceApplyHeaderDTO.setApplyHeaderNumber(applyHeaderNumbers.get(i));
            invoiceApplyHeaderDTO.setDelFlag(0);
            invoiceApplyHeaderDTOMap.put(invoiceApplyHeaderDTO.getApplyHeaderNumber(),invoiceApplyHeaderDTO);
        }
        List<InvoiceApplyHeader> insertedHeaders = invoiceApplyHeaderRepository.batchInsertSelective(new ArrayList<>(invoiceApplyHeaderDTOS));
        for(InvoiceApplyHeader invoiceApplyHeader:insertedHeaders){
            InvoiceApplyHeaderDTO invoiceApplyHeaderDTO=invoiceApplyHeaderDTOMap.get(invoiceApplyHeader.getApplyHeaderNumber());
            invoiceApplyHeaderDTO.setApplyHeaderId(invoiceApplyHeader.getApplyHeaderId());
            for(InvoiceApplyLine invoiceApplyLine:invoiceApplyHeaderDTO.getInvoiceApplyLineList()){
                invoiceApplyLine.setApplyHeaderId(invoiceApplyHeaderDTO.getApplyHeaderId());
            }
        }
    }

    private void  updateHeaders(List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOS){
        Map<String, InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOMap = new HashMap<>();
        for (InvoiceApplyHeaderDTO invoiceApplyHeaderDTO:invoiceApplyHeaderDTOS){
            invoiceApplyHeaderDTOMap.put(invoiceApplyHeaderDTO.getApplyHeaderId().toString(),invoiceApplyHeaderDTO);
            for(InvoiceApplyLine invoiceApplyLine: invoiceApplyHeaderDTO.getInvoiceApplyLineList()){
                invoiceApplyLine.setApplyHeaderId(invoiceApplyHeaderDTO.getApplyHeaderId());
            }
        }
        List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceApplyHeaderRepository.selectByIds(String.join(",",invoiceApplyHeaderDTOMap.keySet()));
        for (InvoiceApplyHeader invoiceApplyHeader :invoiceApplyHeaders){
            InvoiceApplyHeaderDTO invoiceApplyHeaderDTO = invoiceApplyHeaderDTOMap.get(invoiceApplyHeader.getApplyHeaderId().toString());

            BigDecimal oldTotalAmount = invoiceApplyHeaderDTO.getTotalAmount().add(invoiceApplyHeader.getTotalAmount());
            BigDecimal oldTaxAmount = invoiceApplyHeaderDTO.getTaxAmount().add(invoiceApplyHeader.getTaxAmount());
            BigDecimal oldExcludeTaxAmount = invoiceApplyHeaderDTO.getExcludeTaxAmount().add(invoiceApplyHeader.getExcludeTaxAmount());

            invoiceApplyHeaderDTO.setTotalAmount(oldTotalAmount);
            invoiceApplyHeaderDTO.setTaxAmount(oldTaxAmount);
            invoiceApplyHeaderDTO.setTaxAmount(oldExcludeTaxAmount);
        }
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(new ArrayList<>(invoiceApplyHeaderDTOS));

    }

    private void insertLines(List<InvoiceApplyLine> invoiceApplyLines){
        invoiceApplyLineRepository.batchInsertSelective(invoiceApplyLines);
    }

    private  void deleteHeaders(List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOS){
        for(InvoiceApplyHeaderDTO invoiceApplyHeaderDTO:invoiceApplyHeaderDTOS){
            invoiceApplyHeaderDTO.setDelFlag(1);
        }
        invoiceApplyHeaderRepository.batchUpdateOptional(new ArrayList<>(invoiceApplyHeaderDTOS),InvoiceApplyHeader.FIELD_DEL_FLAG);
    }



    InvoiceApplyHeaderDTO getHeaderByCache(Long id){
        String invoiceHeaderJson = redisHelper.strGet( Constants.INVOICE_HEADER_CACHE_PREFIX +id);
        InvoiceApplyHeaderDTO invoiceApplyHeaderDTO = null;
        if(invoiceHeaderJson!= null && !invoiceHeaderJson.isEmpty()) {
            invoiceApplyHeaderDTO = JSON.parseObject(invoiceHeaderJson,InvoiceApplyHeaderDTO.class);
        }
        return invoiceApplyHeaderDTO;
    }

    void setHeaderByCache(InvoiceApplyHeaderDTO invoiceApplyHeaderDTO){
        redisHelper.strSet(Constants.INVOICE_HEADER_CACHE_PREFIX +invoiceApplyHeaderDTO.getApplyHeaderId(),JSON.toJSONString(invoiceApplyHeaderDTO));
    }

    void delHeaderCache(List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOS){
        List<String> redisKeys = invoiceApplyHeaderDTOS.stream().map(header->Constants.INVOICE_HEADER_CACHE_PREFIX+header.getApplyHeaderId()).collect(Collectors.toList());
        redisHelper.delKeys(redisKeys);
    }

    private void populateLines(List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOS){
        Map<String,InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOMap = new HashMap<>();
        Condition condition = new Condition(InvoiceApplyLine.class);
        Condition.Criteria criteria = condition.createCriteria();
        for(InvoiceApplyHeaderDTO invoiceApplyHeaderDTO: invoiceApplyHeaderDTOS){
            invoiceApplyHeaderDTO.setInvoiceApplyLineList(new ArrayList<>());
            String headerId = invoiceApplyHeaderDTO.getApplyHeaderId().toString();
            invoiceApplyHeaderDTOMap.put(headerId,invoiceApplyHeaderDTO);
            criteria.orEqualTo(InvoiceApplyLine.FIELD_APPLY_HEADER_ID,headerId);
        }
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineRepository.selectByCondition(condition);

        for(InvoiceApplyLine invoiceApplyLine:invoiceApplyLines){
            invoiceApplyHeaderDTOMap.get(invoiceApplyLine.getApplyHeaderId().toString()).getInvoiceApplyLineList().add(invoiceApplyLine);
        }
    }
}

