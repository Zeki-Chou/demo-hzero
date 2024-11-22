package com.hand.demo.api.dto;

import lombok.Data;

import java.util.Map;

@Data
public class RezaListHeaderRequestDTO {
    private Map<String, String> requestParam;
    private String jsonString, namespace, serverCode, interfaceCode;
}
