package com.hand.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CountHeaderDTO {
    private String businessKey;
    private String docStatus;
    private Long workflowId;
    private Date approvedTime;
}
