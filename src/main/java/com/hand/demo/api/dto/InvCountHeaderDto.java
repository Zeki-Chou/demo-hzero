package com.hand.demo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvCountHeaderDto {
    private String businessKey;
    private Long workflowId;
    private String docStatus;
    private String approvedTime;
}
