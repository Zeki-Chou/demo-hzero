package com.hand.demo.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.infra.constant.InvoiceApplyConstants;
import lombok.Getter;
import lombok.Setter;
import org.hzero.boot.platform.lov.annotation.LovValue;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
public class InvoiceApplyHeaderDTO extends InvoiceApplyHeader {
    private List<InvoiceApplyLine> headerLines;

    private String applyStatusMeaning;

    private String invoiceColorMeaning;

    private String invoiceTypeMeaning;
}
