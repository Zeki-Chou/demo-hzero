package com.hand.demo.api.controller.v1;

import com.hand.demo.api.controller.dto.InvoiceApplyHeaderDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.export.annotation.ExcelExport;
import org.hzero.export.vo.ExportParam;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * (InvoiceApplyHeader)表控制层
 *
 * @author Allan
 * @since 2024-11-04 14:40:36
 */
@RestController("invoiceApplyHeaderController.v1")
@RequestMapping("/v1/{organizationId}/invoice-apply-headers")
public class InvoiceApplyHeaderController extends BaseController {

    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    private final InvoiceApplyHeaderService invoiceApplyHeaderService;

    public InvoiceApplyHeaderController(InvoiceApplyHeaderRepository invoiceApplyHeaderRepository, InvoiceApplyHeaderService invoiceApplyHeaderService) {
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
        this.invoiceApplyHeaderService = invoiceApplyHeaderService;
    }

    @ApiOperation(value = "apply header list")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @GetMapping
    public ResponseEntity<Page<InvoiceApplyHeaderDTO>> list(InvoiceApplyHeaderDTO invoiceApplyHeader, @PathVariable Long organizationId,
                                                         @ApiIgnore @SortDefault(value = InvoiceApplyHeader.FIELD_APPLY_HEADER_ID, direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<InvoiceApplyHeaderDTO> list = invoiceApplyHeaderService.selectList(pageRequest, invoiceApplyHeader, organizationId);

        return Results.success(list);
    }

    @ApiOperation(value = "apply header detail")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @GetMapping("/{applyHeaderId}/detail")
    public ResponseEntity<InvoiceApplyHeaderDTO> detail(@PathVariable Long applyHeaderId) {
        InvoiceApplyHeaderDTO invoiceApplyHeader = invoiceApplyHeaderService.detail(applyHeaderId);
        return Results.success(invoiceApplyHeader);
    }

    @ApiOperation(value = "create and update apply header")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<InvoiceApplyHeaderDTO>> save(@PathVariable Long organizationId, @RequestBody List<InvoiceApplyHeaderDTO> invoiceApplyHeaders) {
        validObject(invoiceApplyHeaders);
        SecurityTokenHelper.validTokenIgnoreInsert(invoiceApplyHeaders);
        invoiceApplyHeaders.forEach(item -> item.setTenantId(organizationId));
        invoiceApplyHeaderService.saveData(invoiceApplyHeaders, organizationId);
        return Results.success(invoiceApplyHeaders);
    }

    @ApiOperation(value = "Delete data flag")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping
    public ResponseEntity<?> delete(@RequestBody InvoiceApplyHeader invoiceApplyHeader) {
        invoiceApplyHeaderService.deleteData(invoiceApplyHeader);
        return Results.success();
    }

    @ApiOperation(value = "remove apply headers")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<InvoiceApplyHeader> invoiceApplyHeaders) {
        SecurityTokenHelper.validToken(invoiceApplyHeaders);
        invoiceApplyHeaderRepository.batchDeleteByPrimaryKey(invoiceApplyHeaders);
        return Results.success();
    }

    @ApiOperation(value = "export apply headers")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/export-all")
    @ExcelExport(InvoiceApplyHeaderDTO.class)
    public ResponseEntity<List<InvoiceApplyHeaderDTO>> export(@PathVariable Long organizationId, ExportParam exportParam,
                                    HttpServletResponse response) {
        return Results.success(invoiceApplyHeaderService.exportAll(organizationId));
    }

}

