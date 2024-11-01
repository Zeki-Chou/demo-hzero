package com.hand.demo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TranslationRequest {
    private Map<String, String> requestParam;
    private String jsonString, namespace, serverCode, interfaceCode;
}


