package com.hand.demo.api.controller.dto;

import lombok.Data;

import java.util.Date;

@Data
public class WorkFlowDTO {
    private String businessKey;
    private Long workflowId;
    private String docStatus;
    private Date approvedTime;
}
