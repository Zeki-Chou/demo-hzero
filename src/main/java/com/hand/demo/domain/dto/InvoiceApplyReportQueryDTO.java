package com.hand.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceApplyReportQueryDTO{
    private String tenantName;
    private String invoiceNumberFrom;
    private String invoiceNumberTo;
    private String creationDateFrom;
    private String creationDateTo;
    private String submitTimeFrom;
    private String submitTimeTo;
    private String invoiceTypeParam;
    private List<String> listApplyStatus;
    private String listStatusString;
    private List<InvoiceApplyHeaderDTO> listHeader;
}
