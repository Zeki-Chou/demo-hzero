package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.dto.InvoiceApplyLineDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.export.annotation.ExcelExport;
import org.hzero.export.vo.ExportParam;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * (InvoiceApplyLine)表控制层
 *
 * @author
 * @since 2024-11-04 11:44:51
 */

@RestController("invoiceApplyLineController.v1")
@RequestMapping("/v1/{organizationId}/invoice-apply-lines")
public class InvoiceApplyLineController extends BaseController {

    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    private InvoiceApplyLineService invoiceApplyLineService;

    @ApiOperation(value = "列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<Page<InvoiceApplyLine>> list(InvoiceApplyLine invoiceApplyLine, @PathVariable Long organizationId,
                                                       @ApiIgnore @SortDefault(value = InvoiceApplyLine.FIELD_APPLY_LINE_ID,
                                                               direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<InvoiceApplyLine> list = invoiceApplyLineService.selectList(pageRequest, invoiceApplyLine);
        return Results.success(list);
    }

    @ApiOperation(value = "明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{applyLineId}/detail")
    public ResponseEntity<InvoiceApplyLine> detail(@PathVariable Long applyLineId) {
        InvoiceApplyLine invoiceApplyLine = invoiceApplyLineRepository.selectByPrimary(applyLineId);
        return Results.success(invoiceApplyLine);
    }

    @ApiOperation(value = "创建或更新")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<InvoiceApplyLine>> save(@PathVariable Long organizationId, @RequestBody List<InvoiceApplyLine> invoiceApplyLines) {
        validObject(invoiceApplyLines);
//        SecurityTokenHelper.validTokenIgnoreInsert(invoiceApplyLines);
        invoiceApplyLines.forEach(item -> item.setTenantId(organizationId));
        invoiceApplyLineService.saveData(invoiceApplyLines);
        return Results.success(invoiceApplyLines);
    }

    @ApiOperation(value = "删除")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@PathVariable Long organizationId, @RequestBody List<InvoiceApplyLine> invoiceApplyLines) {
//        SecurityTokenHelper.validToken(invoiceApplyLines);
        invoiceApplyLineService.remove(invoiceApplyLines);
        return Results.success();
    }

    @ApiOperation(value = "Export Line")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/export")
    @ExcelExport(InvoiceApplyLineDTO.class)
    public ResponseEntity<Page<InvoiceApplyLineDTO>> export(
            InvoiceApplyLine invoiceApplyLine,
            @PathVariable Long organizationId,
            ExportParam exportParam,
            HttpServletResponse response,
            @ApiIgnore
            @SortDefault(
                    value = InvoiceApplyLine.FIELD_APPLY_LINE_ID,
                    direction = Sort.Direction.DESC
            ) PageRequest pageRequest) {
        Page<InvoiceApplyLineDTO> list = invoiceApplyLineService.exportLine(pageRequest, invoiceApplyLine);
        return Results.success(list);
    }

}

