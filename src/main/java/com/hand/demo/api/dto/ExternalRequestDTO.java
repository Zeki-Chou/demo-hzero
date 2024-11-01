package com.hand.demo.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalRequestDTO {
    private String namespace;
    private String serverCode;
    private String interfaceCode;
}
