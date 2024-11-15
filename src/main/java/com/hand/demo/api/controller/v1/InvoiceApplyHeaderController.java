package com.hand.demo.api.controller.v1;

import com.hand.demo.domain.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.dto.InvoiceApplyReportQueryDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.apaas.common.userinfo.infra.feign.IamRemoteService;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.base.BaseController;
import org.hzero.core.cache.ProcessCacheValue;
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
 * @author azhar.naufal@hand-global.com
 * @since 2024-11-04 10:11:56
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
    public ResponseEntity<Page<InvoiceApplyHeaderDTO>> list(InvoiceApplyHeaderDTO invoiceApplyHeader, @PathVariable Long organizationId,
                                                            @ApiIgnore @SortDefault(value = InvoiceApplyHeader.FIELD_APPLY_HEADER_ID,
                                                                 direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<InvoiceApplyHeaderDTO> list = invoiceApplyHeaderService.selectList(pageRequest, invoiceApplyHeader);
        return Results.success(list);
    }

    @ApiOperation(value = "明细detail")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{applyHeaderId}/detail")
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public ResponseEntity<InvoiceApplyHeaderDTO> detail(@PathVariable Long applyHeaderId) {
        InvoiceApplyHeaderDTO invoiceApplyHeader = invoiceApplyHeaderService.detail(applyHeaderId);
        return Results.success(invoiceApplyHeader);
    }

    @ApiOperation(value = "创建或更新")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<InvoiceApplyHeaderDTO>> save(@PathVariable Long organizationId, @RequestBody List<InvoiceApplyHeaderDTO> invoiceApplyHeaders) {
        validObject(invoiceApplyHeaders);
//        SecurityTokenHelper.validTokenIgnoreInsert(invoiceApplyHeaders);
        invoiceApplyHeaders.forEach(item -> item.setTenantId(organizationId));
        invoiceApplyHeaderService.saveData(invoiceApplyHeaders);
        return Results.success(invoiceApplyHeaders);
    }

    @ApiOperation(value = "删除")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<InvoiceApplyHeader> invoiceApplyHeaders) {
        SecurityTokenHelper.validToken(invoiceApplyHeaders);
        invoiceApplyHeaderRepository.batchDeleteByPrimaryKey(invoiceApplyHeaders);
        return Results.success();
    }

    @ApiOperation(value = "删除soft")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("soft-delete")
    public ResponseEntity<?> softDelete(@RequestParam List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOS) {
//        SecurityTokenHelper.validToken(invoiceApplyHeaders);
        invoiceApplyHeaderService.batchSoftDelete(invoiceApplyHeaderDTOS);
        return Results.success("Success Deleted");
    }

    @ApiOperation(value = "列表export")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("excel-export")
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @ExcelExport(InvoiceApplyHeaderDTO.class)
    public ResponseEntity<Page<InvoiceApplyHeaderDTO>> export(InvoiceApplyHeaderDTO invoiceApplyHeader,
                                                              @PathVariable Long organizationId,
                                                              @ApiIgnore @SortDefault(value = InvoiceApplyHeader.FIELD_APPLY_HEADER_ID,
                                                                      direction = Sort.Direction.DESC) PageRequest pageRequest,
                                                              ExportParam exportParam,
                                                              HttpServletResponse response) {
        Page<InvoiceApplyHeaderDTO> list = invoiceApplyHeaderService.selectListExport(pageRequest, invoiceApplyHeader);
        return Results.success(list);
    }

    @ApiOperation(value = "List For Data Set RTF")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/listDataSet")
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public ResponseEntity<List<InvoiceApplyHeaderDTO>> listDataSet(InvoiceApplyHeaderDTO invoiceApplyHeader, @PathVariable Long organizationId) {
        List<InvoiceApplyHeaderDTO> list = invoiceApplyHeaderService.selectListForDataSet(invoiceApplyHeader);
        return Results.success(list);
    }

    @ApiOperation(value = "List Report Excel")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/listReportExcel")
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public ResponseEntity<List<InvoiceApplyReportQueryDTO>> listReportExcel(InvoiceApplyReportQueryDTO invoiceApplyHeaderDTO, @PathVariable Long organizationId) {
        List<InvoiceApplyReportQueryDTO> list = invoiceApplyHeaderService.selectListForExcel(invoiceApplyHeaderDTO, organizationId);
        return Results.success(list);
    }
}

