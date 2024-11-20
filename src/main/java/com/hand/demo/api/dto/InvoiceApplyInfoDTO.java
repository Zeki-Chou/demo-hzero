package com.hand.demo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceApplyInfoDTO {
    private String invoiceType;
    private String applyHeaderNumberFrom;
    private String applyHeaderNumberTo;
    private Date creationDateFrom;
    private Date creationDateTo;
    private Date submitTimeFrom;
    private Date submitTimeTo;

    private List<String> applyStatusList;
    private String applyStatusMeaning;
    private String tenantName;

    private List<InvoiceApplyInfoDTOOut> invoiceApplyInfoDTOOut;
}