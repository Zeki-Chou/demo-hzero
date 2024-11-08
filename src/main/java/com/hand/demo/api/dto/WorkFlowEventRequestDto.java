package com.hand.demo.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class WorkFlowEventRequestDto {
    private String businessKey;

    private Long workflowId;

    private String docStatus;

    private Date approvedTime;
}
