package com.hand.demo.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class WorkflowStartDTO {
    private String flowKey;
    private String businessKey;
    private String dimension;
    private String starter;
    private Map<String,Object> variableMap;
}
