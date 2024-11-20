package com.hand.demo.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class InvoiceHeaderReportDTO {
    private String invoiceApplyNumberFrom;
    private String invoiceApplyNumberTo;
    private Date invoiceCreationDateFrom;
    private Date invoiceCreationDateTo;
    private Date invoiceSubmitTimeFrom;
    private Date invoiceSubmitTimeTo;
    private List<InvoiceApplyHeaderDTO> listHeader;
    private String userIamNameReport;
    private String tenantNameReport;
    private String invoiceTypeParam;
    private String applyStatusParam;
}
