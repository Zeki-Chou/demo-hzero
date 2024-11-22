package com.hand.demo.api.dto;

import com.hand.demo.infra.constant.Constants;
import lombok.Getter;
import lombok.Setter;
import org.hzero.boot.platform.lov.annotation.LovValue;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class InvoiceApplyHeaderReportDTO {
    private Boolean isPopulatingLines;
    private String userName;
    private String applyHeaderNumberFrom;
    private String applyHeaderNumberTo;
    private Date creationDateFrom;
    private Date creationDateTo;
    private Date submitTimeFrom;
    private Date submitTimeTo;
    private String tenantName;
    private List<String> applyStatusMeanings;
    private List<String> applyStatuses;
    private String applyStatusMulti;
    private String invoiceType;
    private String invoiceTypeMeaning;
    private String invoiceTypeSingle;
    private List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOS;
}
