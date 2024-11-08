package com.hand.demo.infra.util;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utils
 */
public class Utils {
    private Utils() {}
    public static void calcAddInvoiceHeaderAmounts(List<InvoiceApplyHeaderDTO> invoiceApplyHeadersDTOS){
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
    public static void calcAddInvoiceHeaderAmounts(List<InvoiceApplyHeader> invoiceApplyHeaders, List<InvoiceApplyLine> invoiceApplyLines){
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

    public static void calcDelInvoiceHeaderAmounts(List<InvoiceApplyHeader> invoiceApplyHeaders, List<InvoiceApplyLine> invoiceApplyLines){
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

    public static void calcDiffInvoiceHeaderAmounts(List<InvoiceApplyHeader> invoiceApplyHeaders, List<InvoiceApplyLine> invoiceApplyLines,List<InvoiceApplyLine> oldInvoiceApplyLines){
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

    public static void calcInvoiceLineAmounts(List<InvoiceApplyLine> invoiceApplyLines){
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
