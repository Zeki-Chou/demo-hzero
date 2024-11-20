package com.hand.demo.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class InvoiceApplyInfoDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(hidden = true)
    private String tenantName;
    private String invoiceApplyNumberFrom, invoiceApplyNumberTo;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate invoiceCreationDateFrom, invoiceCreationDateTo;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate submitTimeFrom, submitTimeTo;
    private List<String> applyStatus;
    private String invoiceType;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(hidden = true)
    private List<InvoiceApplyHeaderDTO> invoiceApplyHeaderList;
}
