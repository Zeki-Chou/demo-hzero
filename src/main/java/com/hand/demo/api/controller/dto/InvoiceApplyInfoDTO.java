package com.hand.demo.api.controller.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class InvoiceApplyInfoDTO {
    private Date fromSubmitTime;

    private Date toSubmitTime;

    private Date fromCreationDate;

    private Date toCreationDate;

    private String fromApplyHeaderNumber;

    private String toApplyHeaderNumber;

    private String invoiceType;

    private List<String> applyStatusList;

    private String tenantName;

    private List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOS;
}
