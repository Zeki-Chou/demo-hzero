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

    private List<String> applyStatusList;

    private List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOS;
}
