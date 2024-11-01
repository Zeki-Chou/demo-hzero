package com.hand.demo.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class WorkflowEventDTO {
    private String businessKey;
    private String docStatus;
    private Date approvedTime;
    private Long workflowId;
}
