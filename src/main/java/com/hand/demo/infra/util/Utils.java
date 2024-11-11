package com.hand.demo.infra.util;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.dto.InvoiceApplyLineDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.Constants;
import io.choerodon.core.exception.CommonException;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.helper.LanguageHelper;
import org.hzero.core.message.MessageAccessor;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utils
 */
public class Utils {
    private Utils() {}

    public  static class InvoiceApplyLineUtil{
        public static void validate(List<InvoiceApplyLine> invoiceApplyLines, InvoiceApplyLineRepository invoiceApplyLineRepository, InvoiceApplyHeaderRepository invoiceApplyHeaderRepository){
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
        public static void calcAmounts(List<InvoiceApplyLine> invoiceApplyLines){
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
    public static class InvoiceApplyHeaderUtil{
        public static void addAmounts(List<InvoiceApplyHeaderDTO> invoiceApplyHeadersDTOS){
            for(InvoiceApplyHeaderDTO invoiceApplyHeaderDTO:invoiceApplyHeadersDTOS){
                if(invoiceApplyHeaderDTO.getTotalAmount()==null) {
                    invoiceApplyHeaderDTO.setTotalAmount(new BigDecimal(0));
                }
                if(invoiceApplyHeaderDTO.getTaxAmount()==null) {
                    invoiceApplyHeaderDTO.setTaxAmount(new BigDecimal(0));
                }
                if(invoiceApplyHeaderDTO.getExcludeTaxAmount()==null) {
                    invoiceApplyHeaderDTO.setExcludeTaxAmount(new BigDecimal(0));
                }

                for (InvoiceApplyLine invoiceApplyLine: invoiceApplyHeaderDTO.getInvoiceApplyLineList()){
                    BigDecimal totalAmount = invoiceApplyHeaderDTO.getTotalAmount().add(invoiceApplyLine.getTotalAmount());
                    BigDecimal excludeTaxAmount = invoiceApplyHeaderDTO.getExcludeTaxAmount().add(invoiceApplyLine.getExcludeTaxAmount());
                    BigDecimal taxAmount = invoiceApplyHeaderDTO.getTaxAmount().add(invoiceApplyLine.getTaxAmount());

                    invoiceApplyHeaderDTO.setTotalAmount(totalAmount);
                    invoiceApplyHeaderDTO.setExcludeTaxAmount(excludeTaxAmount);
                    invoiceApplyHeaderDTO.setTaxAmount(taxAmount);
                }
            }
        }

        public static void addAmounts(List<InvoiceApplyHeader> invoiceApplyHeaders, List<InvoiceApplyLine> invoiceApplyLines){
            Map<String,InvoiceApplyHeader> invoiceApplyHeaderMap = new HashMap<>();
            for(InvoiceApplyHeader invoiceApplyHeader:invoiceApplyHeaders){
                invoiceApplyHeaderMap.put(invoiceApplyHeader.getApplyHeaderId().toString(),invoiceApplyHeader);
            }

            for(InvoiceApplyLine invoiceApplyLine: invoiceApplyLines){
                InvoiceApplyHeader invoiceApplyHeader =  invoiceApplyHeaderMap.get(invoiceApplyLine.getApplyHeaderId().toString());

                BigDecimal totalAmount = invoiceApplyHeader.getTotalAmount().add(invoiceApplyLine.getTotalAmount());
                BigDecimal excludeTaxAmount = invoiceApplyHeader.getExcludeTaxAmount().add(invoiceApplyLine.getExcludeTaxAmount());
                BigDecimal taxAmount = invoiceApplyHeader.getTaxAmount().add(invoiceApplyLine.getTaxAmount());

                invoiceApplyHeader.setTotalAmount(totalAmount);
                invoiceApplyHeader.setExcludeTaxAmount(excludeTaxAmount);
                invoiceApplyHeader.setTaxAmount(taxAmount);
            }
        }

        public static void subAmounts(List<InvoiceApplyHeader> invoiceApplyHeaders, List<InvoiceApplyLine> invoiceApplyLines){
            Map<String,InvoiceApplyHeader> invoiceApplyHeaderMap = new HashMap<>();
            for(InvoiceApplyHeader invoiceApplyHeader:invoiceApplyHeaders){
                invoiceApplyHeaderMap.put(invoiceApplyHeader.getApplyHeaderId().toString(),invoiceApplyHeader);
            }

            for(InvoiceApplyLine invoiceApplyLine: invoiceApplyLines){
                InvoiceApplyHeader invoiceApplyHeader =  invoiceApplyHeaderMap.get(invoiceApplyLine.getApplyHeaderId().toString());

                BigDecimal totalAmount = invoiceApplyHeader.getTotalAmount().subtract(invoiceApplyLine.getTotalAmount());
                BigDecimal excludeTaxAmount = invoiceApplyHeader.getExcludeTaxAmount().subtract(invoiceApplyLine.getExcludeTaxAmount());
                BigDecimal taxAmount = invoiceApplyHeader.getTaxAmount().subtract(invoiceApplyLine.getTaxAmount());

                invoiceApplyHeader.setTotalAmount(totalAmount);
                invoiceApplyHeader.setExcludeTaxAmount(excludeTaxAmount);
                invoiceApplyHeader.setTaxAmount(taxAmount);
            }
        }

        public static void updateAmounts(List<InvoiceApplyHeader> invoiceApplyHeaders, List<InvoiceApplyLine> invoiceApplyLines,List<InvoiceApplyLine> oldInvoiceApplyLines){
            Map<String,InvoiceApplyHeader> invoiceApplyHeaderMap = new HashMap<>();
            for(InvoiceApplyHeader invoiceApplyHeader:invoiceApplyHeaders){
                invoiceApplyHeaderMap.put(invoiceApplyHeader.getApplyHeaderId().toString(),invoiceApplyHeader);
            }

            Map<String,InvoiceApplyLine> oldInvoiceApplyLineMap = new HashMap<>();
            for(InvoiceApplyLine oldInvoiceApplyLine:oldInvoiceApplyLines){
                oldInvoiceApplyLineMap.put(oldInvoiceApplyLine.getApplyLineId().toString(),oldInvoiceApplyLine);
            }

            for(InvoiceApplyLine invoiceApplyLine:invoiceApplyLines){
                InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderMap.get(invoiceApplyLine.getApplyHeaderId().toString());
                InvoiceApplyLine oldInvoiceApplyLine = oldInvoiceApplyLineMap.get(invoiceApplyLine.getApplyLineId().toString());

                BigDecimal diffTotalAmount = invoiceApplyLine.getTotalAmount().subtract(oldInvoiceApplyLine.getTotalAmount());
                BigDecimal newTotalAmount = invoiceApplyHeader.getTotalAmount().add(diffTotalAmount);
                BigDecimal diffExcludeTaxAmount = invoiceApplyLine.getExcludeTaxAmount().subtract(oldInvoiceApplyLine.getExcludeTaxAmount());
                BigDecimal newExcludeTaxAmount = invoiceApplyHeader.getExcludeTaxAmount().add(diffExcludeTaxAmount);
                BigDecimal diffTaxAmount = invoiceApplyLine.getTaxAmount().subtract(oldInvoiceApplyLine.getTaxAmount());
                BigDecimal newTaxAmount = invoiceApplyHeader.getTaxAmount().add(diffTaxAmount);

                invoiceApplyHeader.setTotalAmount(newTotalAmount);
                invoiceApplyHeader.setExcludeTaxAmount(newExcludeTaxAmount);
                invoiceApplyHeader.setTaxAmount(newTaxAmount);
            }
        }

        public static void validate(List<InvoiceApplyHeader> invoiceApplyHeaders, LovAdapter lovAdapter, InvoiceApplyHeaderRepository invoiceApplyHeaderRepository){

            List<LovValueDTO> applyStatusLov = lovAdapter.queryLovValue(Constants.LOV_INV_APPLY_HEADER_APPLY_STATUS, BaseConstants.DEFAULT_TENANT_ID);
            List<LovValueDTO> invTypeLov = lovAdapter.queryLovValue(Constants.LOV_INV_APPLY_HEADER_INV_TYPE, BaseConstants.DEFAULT_TENANT_ID);
            List<LovValueDTO> invColorLov = lovAdapter.queryLovValue(Constants.LOV_INV_APPLY_HEADER_INV_COLOR, BaseConstants.DEFAULT_TENANT_ID);

            List<String> applyStatuses = applyStatusLov.stream().map(LovValueDTO::getValue).collect(Collectors.toList());
            List<String> invTypes = invTypeLov.stream().map(LovValueDTO::getValue).collect(Collectors.toList());
            List<String> invColors = invColorLov.stream().map(LovValueDTO::getValue).collect(Collectors.toList());

            List<Integer> badHeaderIndex = new ArrayList<>();
            for (int i = 0; i<invoiceApplyHeaders.size(); i++){
                InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaders.get(i);
                if(!applyStatuses.contains(invoiceApplyHeader.getApplyStatus())
                        || !invTypes.contains(invoiceApplyHeader.getInvoiceType())
                        || !invColors.contains(invoiceApplyHeader.getInvoiceColor())){
                    badHeaderIndex.add(i);
                }else if(invoiceApplyHeader.getTotalAmount()!=null
                        || invoiceApplyHeader.getTaxAmount()!=null
                        || invoiceApplyHeader.getExcludeTaxAmount()!=null){
                    badHeaderIndex.add(i);
                } else if (invoiceApplyHeader.getDelFlag()!=null) {
                    badHeaderIndex.add(i);
                }
            }

            if(!badHeaderIndex.isEmpty()){
                Object[] errorMsgArgs = new Object[]{"Bad header request in the following item index: "+ badHeaderIndex};
                String errorMsg = MessageAccessor.getMessage(Constants.MULTILINGUAL_INV_APPLY_HEADER_SAVE_ERROR,errorMsgArgs,Locale.US).getDesc();
                throw  new CommonException(errorMsg);
            }

            Set<String> updateHeaderIdSet = invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() != null).map(header->header.getApplyHeaderId().toString()).collect(Collectors.toSet());
            if(!updateHeaderIdSet.isEmpty()){
                int expectedHeaderSize = updateHeaderIdSet.size();
                int foundHeaderSize = invoiceApplyHeaderRepository.selectByIds(String.join(",", updateHeaderIdSet)).size();
                if(expectedHeaderSize != foundHeaderSize){
                    Object[] errorMsgArgs = new Object[]{"Bad header request in the following item index: "+ badHeaderIndex,"test2","vpn"};
                    String errorMsg = MessageAccessor.getMessage(Constants.MULTILINGUAL_INV_APPLY_HEADER_SAVE_ERROR,errorMsgArgs,Locale.US).getDesc();
                    throw new CommonException(errorMsg);
                }
            }
        }
    }
}
