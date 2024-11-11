package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.InvoiceApplyHeaderDto;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
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
import org.hzero.export.annotation.ExcelColumn;
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
 * @author
 * @since 2024-11-04 14:47:35
 */

@RestController("invoiceApplyHeaderController.v1")
@RequestMapping("/v1/{organizationId}/invoice-apply-headers")
public class InvoiceApplyHeaderController extends BaseController {

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private InvoiceApplyHeaderService invoiceApplyHeaderService;


    @ApiOperation(value = "List")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public ResponseEntity<Page<InvoiceApplyHeaderDto>> list(InvoiceApplyHeaderDto invoiceApplyHeaderDto, @PathVariable Long organizationId,
                                                            @ApiIgnore @SortDefault(value = InvoiceApplyHeader.FIELD_APPLY_HEADER_ID,
                                                                    direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<InvoiceApplyHeaderDto> list = invoiceApplyHeaderService.selectList(pageRequest, invoiceApplyHeaderDto);
        return Results.success(list);
    }

    @ApiOperation(value = "Detail")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{applyHeaderId}/detail")
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public ResponseEntity<InvoiceApplyHeaderDto> detail(@PathVariable Long applyHeaderId) {
        InvoiceApplyHeaderDto detail = invoiceApplyHeaderService.getInvoiceDetailById(applyHeaderId);
        return Results.success(detail);
    }

    @ApiOperation(value = "Insert and Update")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<InvoiceApplyHeaderDto>> save(@PathVariable Long organizationId, @RequestBody List<InvoiceApplyHeaderDto> invoiceApplyHeaderDtos) {
        validObject(invoiceApplyHeaderDtos);
        SecurityTokenHelper.validTokenIgnoreInsert(invoiceApplyHeaderDtos);
        invoiceApplyHeaderDtos.forEach(item -> item.setTenantId(organizationId));
        List<InvoiceApplyHeaderDto> savedDtos = invoiceApplyHeaderService.saveData(invoiceApplyHeaderDtos);
        return Results.success(savedDtos);
    }

    @ApiOperation(value = "Delete")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<InvoiceApplyHeader> invoiceApplyHeaders) {
        invoiceApplyHeaderRepository.batchDeleteByPrimaryKey(invoiceApplyHeaders);
        return Results.success();
    }

    @ApiOperation(value = "Soft Remove")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/soft-remove")
    public ResponseEntity<?> softremove(@RequestBody List<InvoiceApplyHeader> invoiceApplyHeaders) {
        invoiceApplyHeaderService.deleteInvoiceHeaders(invoiceApplyHeaders);
        return ResponseEntity.ok("Invoice headers deleted successfully.");
    }

    @ApiOperation(value = "Export")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/export")
    @ExcelExport(InvoiceApplyHeaderDto.class)
    public ResponseEntity<Page<InvoiceApplyHeaderDto>> export(InvoiceApplyHeaderDto invoiceApplyHeaderDto, @PathVariable Long organizationId,
                                                            @ApiIgnore @SortDefault(value = InvoiceApplyHeader.FIELD_APPLY_HEADER_ID,
                                                                    direction = Sort.Direction.DESC) PageRequest pageRequest, ExportParam exportParam, HttpServletResponse response) {
        Page<InvoiceApplyHeaderDto> list = invoiceApplyHeaderService.selectList(pageRequest, invoiceApplyHeaderDto);
        return Results.success(list);
    }
}

