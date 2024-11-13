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
import org.hzero.core.message.MessageAccessor;
import org.hzero.core.redis.RedisHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.rmi.CORBA.Util;
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
    private InvoiceApplyHeaderServiceImpl invoiceApplyHeaderServiceImpl;
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
    @Transactional(rollbackFor = Exception.class)
    public void saveData(List<InvoiceApplyLineDTO> invoiceApplyLineDTOS) {
        Map<String, InvoiceApplyHeader> invoiceApplyHeaderMap = getHeaderMap(invoiceApplyLineDTOS);
        validate(new ArrayList<>(invoiceApplyLineDTOS),invoiceApplyLineRepository,invoiceApplyHeaderRepository);

        calcAmounts(new ArrayList<>(invoiceApplyLineDTOS));

        List<InvoiceApplyLineDTO> insertLineDTOS = invoiceApplyLineDTOS.stream().filter(line -> line.getApplyLineId() == null).collect(Collectors.toList());
        List<InvoiceApplyLineDTO> updateLineDTOS = invoiceApplyLineDTOS.stream().filter(line -> line.getApplyLineId() != null).collect(Collectors.toList());

        if(!insertLineDTOS.isEmpty()) {
            insertLines(invoiceApplyHeaderMap, insertLineDTOS);
        }
        if(!updateLineDTOS.isEmpty()) {
            updateLines(invoiceApplyHeaderMap, updateLineDTOS);
        }
        updateHeaders(invoiceApplyHeaderMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public  void delete(List<InvoiceApplyLineDTO> invoiceApplyLines){
        Map<String, InvoiceApplyHeader> invoiceApplyHeaderMap = getHeaderMap(invoiceApplyLines);
        validate(new ArrayList<>(invoiceApplyLines),invoiceApplyLineRepository,invoiceApplyHeaderRepository);
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
        String lineIds = invoiceApplyLineDTOS.stream().map(line->line.getApplyLineId().toString()).collect(Collectors.joining(","));
        List<InvoiceApplyLine> oldInvoiceApplyLines = invoiceApplyLineRepository.selectByIds(lineIds);
        invoiceApplyLineRepository.batchDeleteByPrimaryKey(new ArrayList<>(invoiceApplyLineDTOS));
        invoiceApplyHeaderServiceImpl.subAmounts(new ArrayList<>(invoiceApplyHeaderMap.values()),oldInvoiceApplyLines);
    }

    private  void insertLines(Map<String, InvoiceApplyHeader> invoiceApplyHeaderMap,List<InvoiceApplyLineDTO> invoiceApplyLineDTOS){
        if(invoiceApplyLineDTOS.isEmpty()){
            return;
        }
        calcAmounts(new ArrayList<>(invoiceApplyLineDTOS));
        invoiceApplyHeaderServiceImpl.addAmounts(new ArrayList<>(invoiceApplyHeaderMap.values()),new ArrayList<>(invoiceApplyLineDTOS));
        invoiceApplyLineRepository.batchInsertSelective(new ArrayList<>(invoiceApplyLineDTOS));
    }

    private  void updateLines(Map<String, InvoiceApplyHeader> invoiceApplyHeaderMap,List<InvoiceApplyLineDTO> invoiceApplyLineDTOS){
        if(invoiceApplyLineDTOS.isEmpty()){
            return;
        }
        String oldLineIds = invoiceApplyLineDTOS.stream().map(line -> line.getApplyLineId().toString()).collect(Collectors.joining(","));
        List<InvoiceApplyLine> oldLines = invoiceApplyLineRepository.selectByIds(oldLineIds);
        calcAmounts(new ArrayList<>(invoiceApplyLineDTOS));
        invoiceApplyHeaderServiceImpl.updateAmounts(new ArrayList<>(invoiceApplyHeaderMap.values()),new ArrayList<>(invoiceApplyLineDTOS),oldLines);
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

    public  void validate(List<InvoiceApplyLine> invoiceApplyLines, InvoiceApplyLineRepository invoiceApplyLineRepository, InvoiceApplyHeaderRepository invoiceApplyHeaderRepository){
        Set<String> headerIdSet =invoiceApplyLines.stream().filter(line->line.getApplyHeaderId() != null).map(line->line.getApplyHeaderId().toString()).collect(Collectors.toSet());
        if(!headerIdSet.isEmpty()) {
            int expectedHeaderSize = headerIdSet.size();
            int foundHeaderSize = invoiceApplyHeaderRepository.selectByIds(String.join(",", headerIdSet)).size();
            if (expectedHeaderSize != foundHeaderSize) {
                Object[] errorMsgArgs = new Object[]{"Expected header size not equal to found header size"};
                String errorMsg = MessageAccessor.getMessage(Constants.MULTILINGUAL_INV_APPLY_LINE_SAVE_ERROR, errorMsgArgs, Locale.US).getDesc();
                throw new CommonException(errorMsg);
            }
        }

        List<Integer> badLineIndex = new ArrayList<>();
        for (int i=0;i< invoiceApplyLines.size();i++){
            if(invoiceApplyLines.get(i).getTotalAmount() != null
                    || invoiceApplyLines.get(i).getTaxAmount() != null
                    || invoiceApplyLines.get(i).getExcludeTaxAmount() != null){
                badLineIndex.add(i);
            }
        }
        if(!badLineIndex.isEmpty()){
            Object[] errorMsgArgs = new Object[]{"Lines should have no total amount, tax amount, and exclude tax amount values. Bad lines index: "+badLineIndex};
            String errorMsg = MessageAccessor.getMessage(Constants.MULTILINGUAL_INV_APPLY_LINE_SAVE_ERROR,errorMsgArgs,Locale.US).getDesc();
            throw new CommonException(errorMsg);
        }

        Set<String> updateLineIdSet = invoiceApplyLines.stream().filter(line ->line.getApplyLineId() != null).map(line-> line.getApplyLineId().toString()).collect(Collectors.toSet());
        if(!updateLineIdSet.isEmpty()){
            int expectedLineSize = updateLineIdSet.size();
            int foundLineSize = invoiceApplyLineRepository.selectByIds(String.join(",", updateLineIdSet)).size();
            if(expectedLineSize != foundLineSize){
                Object[] errorMsgArgs = new Object[]{"Expected update line size not equal to found line size"};
                String errorMsg = MessageAccessor.getMessage(Constants.MULTILINGUAL_INV_APPLY_LINE_SAVE_ERROR,errorMsgArgs,Locale.US).getDesc();
                throw new CommonException(errorMsg);
            }
        }
    }
    public  void calcAmounts(List<InvoiceApplyLine> invoiceApplyLines){
        for(InvoiceApplyLine invoiceApplyLine: invoiceApplyLines){
            BigDecimal totalAmount = invoiceApplyLine.getUnitPrice().multiply(invoiceApplyLine.getQuantity());
            BigDecimal taxAmount = totalAmount.multiply(invoiceApplyLine.getTaxRate());
            BigDecimal excludeTaxAmount = totalAmount.subtract(taxAmount);

            invoiceApplyLine.setTotalAmount(totalAmount);
            invoiceApplyLine.setTaxAmount(taxAmount);
            invoiceApplyLine.setExcludeTaxAmount(excludeTaxAmount);
        }
    }
}

