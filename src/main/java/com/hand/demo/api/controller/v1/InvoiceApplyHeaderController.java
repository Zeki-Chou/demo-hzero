package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
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
 * @author Fatih Khoiri
 * @since 2024-11-04 10:14:16
 */

@RestController("invoiceApplyHeaderController.v1")
@RequestMapping("/v1/{organizationId}/invoice-apply-headers")
public class InvoiceApplyHeaderController extends BaseController {

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private InvoiceApplyHeaderService invoiceApplyHeaderService;

    @ApiOperation(value = "Get List")
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
    @GetMapping("/{applyHeaderId}/detail")
    public ResponseEntity<InvoiceApplyHeaderDTO> detail(@PathVariable Long applyHeaderId) {
        InvoiceApplyHeaderDTO dto = invoiceApplyHeaderService.detail(applyHeaderId);
        return Results.success(dto);
    }

    @ApiOperation(value = "Save Data")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<InvoiceApplyHeader>> save(@PathVariable Long organizationId, @RequestBody List<InvoiceApplyHeader> invoiceApplyHeaders) {
        validObject(invoiceApplyHeaders);
//        SecurityTokenHelper.validTokenIgnoreInsert(invoiceApplyHeaders);
        invoiceApplyHeaders.forEach(item -> item.setTenantId(organizationId));
//        invoiceApplyHeaderService.saveData(invoiceApplyHeaders);
        return Results.success(invoiceApplyHeaders);
    }

    @ApiOperation(value = "Save Data List Lines")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/list-lines")
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public ResponseEntity<List<InvoiceApplyHeaderDTO>> saveList(@PathVariable Long organizationId, @RequestBody List<InvoiceApplyHeaderDTO> invoiceApplyHeaders) {
        validObject(invoiceApplyHeaders);
//        SecurityTokenHelper.validTokenIgnoreInsert(invoiceApplyHeaders);
        invoiceApplyHeaders.forEach(item -> item.setTenantId(organizationId));
        invoiceApplyHeaderService.saveData(invoiceApplyHeaders);
        return Results.success(invoiceApplyHeaders);
    }

    @ApiOperation(value = "Delete")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<InvoiceApplyHeader> invoiceApplyHeaders) {
//        SecurityTokenHelper.validToken(invoiceApplyHeaders);
        invoiceApplyHeaderRepository.batchDeleteByPrimaryKey(invoiceApplyHeaders);
        return Results.success();
    }

    @ApiOperation(value = "Delete by id")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/delete-id")
    public ResponseEntity<?> delete(@RequestParam Long headerId) {
//        SecurityTokenHelper.validToken(invoiceApplyHeaders);
        InvoiceApplyHeaderDTO dto = invoiceApplyHeaderService.delete(headerId);
        return Results.success(dto);
    }

    @ApiOperation(value = "Get List Excel")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("get-excel")
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @ExcelExport(InvoiceApplyHeaderDTO.class)
    public ResponseEntity<Page<InvoiceApplyHeaderDTO>> listExcel(InvoiceApplyHeader invoiceApplyHeader,
                                                                 @PathVariable Long organizationId,
                                                                 @ApiIgnore @SortDefault(value = InvoiceApplyHeader.FIELD_APPLY_HEADER_ID,
                                                                         direction = Sort.Direction.DESC) PageRequest pageRequest,
                                                                 ExportParam exportParam,
                                                                 HttpServletResponse httpServletResponse) {
        Page<InvoiceApplyHeaderDTO> list = invoiceApplyHeaderService.selectList(pageRequest, invoiceApplyHeader);
        return Results.success(list);
    }

}

