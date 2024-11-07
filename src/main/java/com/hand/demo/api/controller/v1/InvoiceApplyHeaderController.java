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
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * (InvoiceApplyHeader)表控制层
 *
 * @author
 * @since 2024-11-04 11:43:30
 */

@RestController("invoiceApplyHeaderController.v1")
@RequestMapping("/v1/{organizationId}/invoice-apply-headers")
public class InvoiceApplyHeaderController extends BaseController {

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private InvoiceApplyHeaderService invoiceApplyHeaderService;

    @ApiOperation(value = "列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public ResponseEntity<Page<InvoiceApplyHeaderDTO>> list(InvoiceApplyHeader invoiceApplyHeader, @PathVariable Long organizationId,
                                                            @ApiIgnore @SortDefault(value = InvoiceApplyHeader.FIELD_APPLY_HEADER_ID,
                                                                    direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<InvoiceApplyHeaderDTO> list = invoiceApplyHeaderService.selectList(pageRequest, invoiceApplyHeader);
        return Results.success(list);
    }

    @ApiOperation(value = "明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{applyHeaderId}/detail")
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public ResponseEntity<InvoiceApplyHeaderDTO> detail(@PathVariable Long organizationId, @PathVariable Long applyHeaderId) {
        return Results.success(invoiceApplyHeaderService.detail(applyHeaderId));
    }

    @ApiOperation(value = "创建或更新")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<InvoiceApplyHeaderDTO>> save(@PathVariable Long organizationId, @RequestBody List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOs) {
        validObject(invoiceApplyHeaderDTOs);
        SecurityTokenHelper.validTokenIgnoreInsert(invoiceApplyHeaderDTOs);
        invoiceApplyHeaderDTOs.forEach(item -> item.setTenantId(organizationId));
        invoiceApplyHeaderService.saveData(invoiceApplyHeaderDTOs);
        return Results.success(invoiceApplyHeaderDTOs);
    }

    @ApiOperation(value = "删除")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<InvoiceApplyHeader> invoiceApplyHeaders) {
        SecurityTokenHelper.validToken(invoiceApplyHeaders);
        invoiceApplyHeaderRepository.batchDeleteByPrimaryKey(invoiceApplyHeaders);
        return Results.success();
    }

    @ApiOperation(value = "Soft delete invoice apply header")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/soft-delete/{applyHeaderId}")
    public ResponseEntity<?> softDeleteById(@PathVariable("organizationId") Long organizationId, @PathVariable Long applyHeaderId) {
        invoiceApplyHeaderService.softDeleteById(applyHeaderId);
        return Results.success();
    }
}

