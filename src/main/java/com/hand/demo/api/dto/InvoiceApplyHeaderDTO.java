package com.hand.demo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;
import org.json.JSONObject;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@ApiModel("Export")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "EXAM-EXPORTHEADER-47356")
@ExcelSheet(en = "EXPORTHEADER-47356")
public class InvoiceApplyHeaderDTO extends InvoiceApplyHeader {

    @ExcelColumn(en = "applyStatusMeaning")
    @Column(name = "applyStatusMeaning")
    private String applyStatusMeaning;

    @ExcelColumn(en = "invoiceColorMeaning")
    @Column(name = "invoiceColorMeaning")
    private String invoiceColorMeaning;

    @ExcelColumn(en = "invoiceTypeMeaning")
    @Column(name = "invoiceTypeMeaning")
    private String invoiceTypeMeaning;

    private Boolean tenantAdminFlag;

    private String createdName;

    @ExcelColumn(en = "invoiceApplyLines")
    @Column(name = "invoiceApplyLines")
    private List<InvoiceApplyLine> invoiceApplyLines;

    public InvoiceApplyHeaderDTO(String applyStatusMeaning, String invoiceColorMeaning, String invoiceTypeMeaning, List<InvoiceApplyLine> invoiceApplyLines, Boolean tenantAdminFlag, String createdName) {
        this.applyStatusMeaning = applyStatusMeaning;
        this.invoiceColorMeaning = invoiceColorMeaning;
        this.invoiceTypeMeaning = invoiceTypeMeaning;
        this.invoiceApplyLines = invoiceApplyLines;
        this.tenantAdminFlag = tenantAdminFlag;
        this.createdName = createdName;
    }

    public InvoiceApplyHeaderDTO() {}
}
