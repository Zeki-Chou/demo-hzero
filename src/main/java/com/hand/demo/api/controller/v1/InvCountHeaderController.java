package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.InvCountHeaderDto;
import com.hand.demo.api.dto.StartWorkflowRequest;
import com.hand.demo.api.dto.WithdrawWorkflowRequest;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.workflow.dto.RunInstance;
import org.hzero.boot.workflow.dto.RunTaskHistory;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hand.demo.app.service.InvCountHeaderService;
import com.hand.demo.domain.entity.InvCountHeader;
import com.hand.demo.domain.repository.InvCountHeaderRepository;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * Inventory Count Header Table(InvCountHeader)表控制层
 *
 * @author
 * @since 2024-10-25 13:59:58
 */

@RestController("invCountHeaderController.v1")
@RequestMapping("/v1/{organizationId}/inv-count-headers")
public class InvCountHeaderController extends BaseController {

    @Autowired
    private InvCountHeaderRepository invCountHeaderRepository;

    @Autowired
    private InvCountHeaderService invCountHeaderService;

    @ApiOperation(value = "Inventory Count Header Table列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<Page<InvCountHeader>> list(InvCountHeader invCountHeader, @PathVariable Long organizationId,
                                                     @ApiIgnore @SortDefault(value = InvCountHeader.FIELD_COUNT_HEADER_ID,
                                                             direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<InvCountHeader> list = invCountHeaderService.selectList(pageRequest, invCountHeader);
        return Results.success(list);
    }

    @ApiOperation(value = "Inventory Count Header Table明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{countHeaderId}/detail")
    public ResponseEntity<InvCountHeader> detail(@PathVariable Long countHeaderId) {
        InvCountHeader invCountHeader = invCountHeaderRepository.selectByPrimary(countHeaderId);
        return Results.success(invCountHeader);
    }

    @ApiOperation(value = "创建或更新Inventory Count Header Table")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<InvCountHeader>> save(@PathVariable Long organizationId, @RequestBody List<InvCountHeader> invCountHeaders) {
        validObject(invCountHeaders);
        SecurityTokenHelper.validTokenIgnoreInsert(invCountHeaders) ;
        invCountHeaders.forEach(item -> item.setTenantId(organizationId));
        invCountHeaderService.saveData(invCountHeaders);
        return Results.success(invCountHeaders);
    }

    @ApiOperation(value = "删除Inventory Count Header Table")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<InvCountHeader> invCountHeaders) {
        SecurityTokenHelper.validToken(invCountHeaders);
        invCountHeaderRepository.batchDeleteByPrimaryKey(invCountHeaders);
        return Results.success();
    }

    @ApiOperation(value = "Count Callback")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/approval-callback")
    public ResponseEntity<InvCountHeader> approvalCallback(
            @PathVariable Long organizationId,
            @RequestBody InvCountHeaderDto invCountHeadersDto
    ) {
        return Results.success(invCountHeaderService.approvalCallback(organizationId, invCountHeadersDto));
    }

    @ApiOperation(value = "Approval history")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/approve-history/{instanceId}")
    public ResponseEntity<List<RunTaskHistory>> approveHistory(
            @PathVariable Long organizationId,
            @PathVariable("instanceId") Long instanceId
    ) {
        return Results.success(invCountHeaderService.showApproveHistory(organizationId, instanceId));
    }

    @ApiOperation(value = "Start workflow")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/start-workflow")
    public ResponseEntity<RunInstance> startWorkflow(
            @PathVariable Long organizationId,
            @RequestBody StartWorkflowRequest request
            ) {
        return Results.success(invCountHeaderService.startWorkflow(organizationId, request));
    }

    @ApiOperation(value = "Withdraw workflow")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/withdraw-workflow")
    public ResponseEntity<String> withdrawWorkflow(
            @PathVariable Long organizationId,
            @RequestBody WithdrawWorkflowRequest request
    ) {
        invCountHeaderService.withdrawWorkflow(organizationId, request);
        return Results.success("Success");
    }
}

