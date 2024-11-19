package com.hand.demo.api.controller.dto;

import java.util.Date;
import java.util.List;

public class InvoiceApplyInfoDTO {
    private Date fromSubmitTime;

    private Date toSubmitTime;

    private Date fromCreationDate;

    private Date toCreationDate;

    private String fromApplyHeaderNumber;

    private String toApplyHeaderNumber;

    private List<String> applyStatusList;

    private InvoiceApplyHeaderDTO invoiceApplyHeaderDTO;
}
