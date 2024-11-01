package com.hand.demo.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WorkflowWithdrawDTO {
    private List<Long> instanceIds;
    private Boolean checkFlag;
}
