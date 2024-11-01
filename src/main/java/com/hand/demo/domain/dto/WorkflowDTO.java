package com.hand.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDTO {
    private Long tenantId;
    private String flowKey;
    private String businessKey;
    private String dimension;
    private String starter;
    private Map<String, Object> variableMap;

}
