package com.hand.demo.api.controller.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.export.annotation.ExcelExport;
import org.hzero.export.annotation.ExcelSheet;
import org.hzero.export.vo.ExportParam;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import springfox.documentation.annotations.ApiIgnore;

import javax.persistence.Table;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * (InvoiceApplyHeader)表控制层
 *
 * @author
 * @since 2024-11-04 10:16:08
 */

@RestController("invoiceApplyHeaderController.v1")
@RequestMapping("/v1/{organizationId}/invoice-apply-headers")
public class InvoiceApplyHeaderController extends BaseController {

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private InvoiceApplyHeaderService invoiceApplyHeaderService;

    @ApiOperation(value = "Get All List")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public ResponseEntity<Page<InvoiceApplyHeaderDTO>> list(InvoiceApplyHeader invoiceApplyHeader, @PathVariable Long organizationId,
                                                            @ApiIgnore @SortDefault(value = InvoiceApplyHeader.FIELD_APPLY_HEADER_ID,
                                                                 direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<InvoiceApplyHeaderDTO> list = invoiceApplyHeaderService.selectList(pageRequest, invoiceApplyHeader);
        return Results.success(list);
    }

    @ApiOperation(value = "Get Detail")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{headerId}/detail")
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public ResponseEntity<InvoiceApplyHeaderDTO> detail(@PathVariable Long organizationId, @PathVariable Long headerId) {
        InvoiceApplyHeaderDTO invoiceApplyHeader = invoiceApplyHeaderService.detail(headerId);
        return Results.success(invoiceApplyHeader);
    }

    @ApiOperation(value = "Save Data")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<InvoiceApplyHeaderDTO>> save(@PathVariable Long organizationId, @RequestBody List<InvoiceApplyHeaderDTO> invoiceApplyHeaders) {
        validObject(invoiceApplyHeaders);
//        SecurityTokenHelper.validTokenIgnoreInsert(invoiceApplyHeaders);
        invoiceApplyHeaders.forEach(item -> item.setTenantId(organizationId));
        invoiceApplyHeaderService.saveData(invoiceApplyHeaders);
        return Results.success(invoiceApplyHeaders);
    }

//    @ApiOperation(value = "Remove")
//    @Permission(level = ResourceLevel.ORGANIZATION)
//    @DeleteMapping
//    public ResponseEntity<?> remove(@RequestBody List<InvoiceApplyHeader> invoiceApplyHeaders) {
//        SecurityTokenHelper.validToken(invoiceApplyHeaders);
//        invoiceApplyHeaderRepository.batchDeleteByPrimaryKey(invoiceApplyHeaders);
//        return Results.success();
//    }

    @ApiOperation(value = "Delete Data")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/{headerId}")
    public void remove(@PathVariable Long organizationId, @PathVariable Long headerId) {
        invoiceApplyHeaderService.deleteData(headerId);
    }

    @ApiOperation(value = "Export")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/export-all")
    @ExcelExport(InvoiceApplyHeaderDTO.class)
    public ResponseEntity<List<InvoiceApplyHeaderDTO>> complexSelect(@PathVariable Long organizationId, ExportParam exportParam, HttpServletResponse response, PageRequest pageRequest) {
        return Results.success(invoiceApplyHeaderService.exportAll(pageRequest));
    }
}

