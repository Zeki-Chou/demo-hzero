package com.hand.demo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

import javax.persistence.Column;
import javax.persistence.Table;

@Getter
@Setter
@ApiModel("Export")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "EXAM-EXPORTLINE-47356")
@ExcelSheet(en = "EXPORTLINE-47356")
public class InvoiceApplyLineDTO extends InvoiceApplyLine {

    @ExcelColumn(en = "apply_header_number")
    private String headerNumber;

    public InvoiceApplyLineDTO(String headerNumber) {
        this.headerNumber = headerNumber;
    }

    public InvoiceApplyLineDTO() {

    }
}
