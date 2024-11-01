package com.hand.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hzero.boot.interfaces.sdk.dto.InterfaceDTO;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ExternalInterfaceDTO {
    private String namespace;
    private String serverCode;
    private String interfaceCode;
}
