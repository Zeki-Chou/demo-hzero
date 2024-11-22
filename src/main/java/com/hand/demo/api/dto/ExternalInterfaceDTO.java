package com.hand.demo.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ExternalInterfaceDTO {
    private String namespace;
    private String serverCode;
    private String interfaceCode;
    private Map<String,String> requestMap;
    private Map<String,String> pathMap;
    private Map<String,String> heaederMap;
    private String payload;
    private String mediaType;
}
