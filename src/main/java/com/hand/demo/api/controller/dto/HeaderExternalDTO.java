package com.hand.demo.api.controller.dto;

import lombok.Data;

@Data
public class HeaderExternalDTO extends ServiceTemplateDTO {
    Long applyHeaderId;
    String token;
}
