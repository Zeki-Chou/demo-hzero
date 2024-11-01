package com.hand.demo.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hzero.boot.workflow.dto.RunInstance;

import java.util.Collections;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StartWorkflowRequest {
    private String flowKey;
    private String businessKey;
    private String dimension;
    private String starter;
    private Map<String, Object> variableMap = Collections.emptyMap();
}

