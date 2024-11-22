package com.hand.demo.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InfoHeaderDTO {
    private String errorMsg;
    private String successMsg;
    private List<InvoiceApplyHeaderDTO> errorMsgList;
}
