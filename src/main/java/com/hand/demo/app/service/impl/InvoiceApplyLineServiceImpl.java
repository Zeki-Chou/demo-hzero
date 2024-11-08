package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.InvoiceApplyLineDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.Constants;
import com.hand.demo.infra.util.Utils;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.core.redis.RedisHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyLine)应用服务
 *
 * @author lareza.farhan@hand-global.com
 * @since 2024-11-04 10:14:07
 */
@Service
public class InvoiceApplyLineServiceImpl implements InvoiceApplyLineService {
    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    RedisHelper redisHelper;

    @Override
    public Page<InvoiceApplyLineDTO> selectList(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {
        Page<InvoiceApplyLine> invoiceApplyHeaderPage = PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyLineRepository.selectList(invoiceApplyLine));

        List<InvoiceApplyLineDTO> invoiceApplyLineDTOS = new ArrayList<>();
        for (InvoiceApplyLine invoiceApplyLineInPage: invoiceApplyHeaderPage.getContent()){
            InvoiceApplyLineDTO invoiceApplyLineDTO = new InvoiceApplyLineDTO();
            BeanUtils.copyProperties(invoiceApplyLineInPage,invoiceApplyLineDTO);
            invoiceApplyLineDTOS.add(invoiceApplyLineDTO);
        }
        populateHeaderNumber(invoiceApplyLineDTOS);

        Page<InvoiceApplyLineDTO> invoiceApplyLineDTOPage = new Page<>();
        BeanUtils.copyProperties(invoiceApplyHeaderPage,invoiceApplyLineDTOPage);
        invoiceApplyLineDTOPage.setContent(invoiceApplyLineDTOS);
        return invoiceApplyLineDTOPage;
    }


    @Override
    public void saveData(List<InvoiceApplyLineDTO> invoiceApplyLineDTOS) {
        Map<String, InvoiceApplyHeader> invoiceApplyHeaderMap = getHeaderMap(invoiceApplyLineDTOS);
        validate(invoiceApplyHeaderMap,invoiceApplyLineDTOS);
        Utils.calcInvoiceLineAmounts(new ArrayList<>(invoiceApplyLineDTOS));

        List<InvoiceApplyLineDTO> insertLineDTOS = invoiceApplyLineDTOS.stream().filter(line -> line.getApplyLineId() == null).collect(Collectors.toList());
        List<InvoiceApplyLineDTO> updateLineDTOS = invoiceApplyLineDTOS.stream().filter(line -> line.getApplyLineId() != null).collect(Collectors.toList());

        insertLines(invoiceApplyHeaderMap, insertLineDTOS);
        updateLines(invoiceApplyHeaderMap, updateLineDTOS);
        updateHeaders(invoiceApplyHeaderMap);
    }

    @Override
    public  void delete(List<InvoiceApplyLineDTO> invoiceApplyLines){
        Map<String, InvoiceApplyHeader> invoiceApplyHeaderMap = getHeaderMap(invoiceApplyLines);
        deleteLines(invoiceApplyHeaderMap,invoiceApplyLines);
        updateHeaders(invoiceApplyHeaderMap);
    }

    private Map<String, InvoiceApplyHeader>  getHeaderMap(List<InvoiceApplyLineDTO> invoiceApplyLineDTOS){
        String headerIds = invoiceApplyLineDTOS.stream().map(line-> line.getApplyHeaderId().toString()).collect(Collectors.joining(","));
        List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceApplyHeaderRepository.selectByIds(headerIds);
        Map<String, InvoiceApplyHeader> invoiceApplyHeaderMap = new HashMap<>();
        for (InvoiceApplyHeader invoiceApplyHeader : invoiceApplyHeaders) {
            invoiceApplyHeaderMap.put(invoiceApplyHeader.getApplyHeaderId().toString(), invoiceApplyHeader);
        }
        return invoiceApplyHeaderMap;
    }

    private void deleteLines(Map<String, InvoiceApplyHeader> invoiceApplyHeaderMap,List<InvoiceApplyLineDTO> invoiceApplyLineDTOS){
        invoiceApplyLineRepository.batchDeleteByPrimaryKey(new ArrayList<>(invoiceApplyLineDTOS));
        Utils.calcDelInvoiceHeaderAmounts(new ArrayList<>(invoiceApplyHeaderMap.values()),new ArrayList<>(invoiceApplyLineDTOS));
    }

    private  void insertLines(Map<String, InvoiceApplyHeader> invoiceApplyHeaderMap,List<InvoiceApplyLineDTO> invoiceApplyLineDTOS){
        if(invoiceApplyLineDTOS.isEmpty()){
            return;
        }
        Utils.calcInvoiceLineAmounts(new ArrayList<>(invoiceApplyLineDTOS));
        Utils.calcAddInvoiceHeaderAmounts(new ArrayList<>(invoiceApplyHeaderMap.values()),new ArrayList<>(invoiceApplyLineDTOS));
        invoiceApplyLineRepository.batchInsertSelective(new ArrayList<>(invoiceApplyLineDTOS));
    }

    private  void updateLines(Map<String, InvoiceApplyHeader> invoiceApplyHeaderMap,List<InvoiceApplyLineDTO> invoiceApplyLineDTOS){
        if(invoiceApplyLineDTOS.isEmpty()){
            return;
        }
        String oldLineIds = invoiceApplyLineDTOS.stream().map(line -> line.getApplyLineId().toString()).collect(Collectors.joining(","));
        List<InvoiceApplyLine> oldLines = invoiceApplyLineRepository.selectByIds(oldLineIds);
        Utils.calcInvoiceLineAmounts(new ArrayList<>(invoiceApplyLineDTOS));
        Utils.calcDiffInvoiceHeaderAmounts(new ArrayList<>(invoiceApplyHeaderMap.values()),new ArrayList<>(invoiceApplyLineDTOS),oldLines);
        invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(new ArrayList<>(invoiceApplyLineDTOS));
    }


    private void updateHeaders(Map<String,InvoiceApplyHeader> invoiceApplyHeaderMap){
        invoiceApplyHeaderRepository.batchUpdateOptional(new ArrayList<>(invoiceApplyHeaderMap.values()),InvoiceApplyHeader.FIELD_TOTAL_AMOUNT,InvoiceApplyHeader.FIELD_EXCLUDE_TAX_AMOUNT,InvoiceApplyHeader.FIELD_TAX_AMOUNT);
        List<String> redisKeys = invoiceApplyHeaderMap.keySet().stream().map(headerId -> Constants.INVOICE_HEADER_CACHE_PREFIX + headerId).collect(Collectors.toList());
        redisHelper.delKeys(redisKeys);
    }
    private void populateHeaderNumber(List<InvoiceApplyLineDTO> invoiceApplyLineDTOS){
        String headerIds = invoiceApplyLineDTOS.stream().map(line->line.getApplyHeaderId().toString()).collect(Collectors.joining(","));
        List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceApplyHeaderRepository.selectByIds(headerIds);
        Map<String, InvoiceApplyHeader> invoiceApplyHeaderMap = new HashMap<>();
        for(InvoiceApplyHeader invoiceApplyHeader:invoiceApplyHeaders){
            invoiceApplyHeaderMap.put(invoiceApplyHeader.getApplyHeaderId().toString(),invoiceApplyHeader);
        }
        for(InvoiceApplyLineDTO invoiceApplyLineDTO:invoiceApplyLineDTOS){
            String invoiceApplyHeaderNumber = invoiceApplyHeaderMap.get(invoiceApplyLineDTO.getApplyHeaderId().toString()).getApplyHeaderNumber();
            invoiceApplyLineDTO.setInvoiceApplyHeaderNumber(invoiceApplyHeaderNumber);
        }
    }
    private void validate(Map<String,InvoiceApplyHeader> invoiceApplyHeaderMap,List<InvoiceApplyLineDTO> invoiceApplyLineDTOS){
        int expectedHeaderSize = invoiceApplyLineDTOS.stream().map(line->line.getApplyHeaderId().toString()).collect(Collectors.toSet()).size();
        if(invoiceApplyHeaderMap.keySet().size()!=expectedHeaderSize){
            throw  new CommonException(Constants.MULTILINGUAL_INV_APPLY_LINE_SAVE_ERROR, "Header Ids not found or deleted");
        }

        List<Integer> badInvoiceApplyHeaderIndex = new ArrayList<>();
        for (int i=0;i< invoiceApplyLineDTOS.size();i++){
            if(invoiceApplyLineDTOS.get(i).getTotalAmount() != null
                || invoiceApplyLineDTOS.get(i).getTaxAmount() != null
                || invoiceApplyLineDTOS.get(i).getExcludeTaxAmount() != null){
                badInvoiceApplyHeaderIndex.add(i);
            }
        }
        if(!badInvoiceApplyHeaderIndex.isEmpty()){
            throw  new CommonException(Constants.MULTILINGUAL_INV_APPLY_LINE_SAVE_ERROR, "Lines should have no total amount, tax amount, and exclude tax amount values. Bad lines index: "+badInvoiceApplyHeaderIndex);
        }
    }


}

