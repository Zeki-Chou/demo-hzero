package com.hand.demo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExternalDTO {
    private String headerId;
    private String nameSpace;
    private String serverCode;
    private String interfaceCode;
    private String authorization;
}
