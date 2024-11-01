package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.PurchaseOrderDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hand.demo.app.service.PurchaseOrderService;
import com.hand.demo.domain.entity.PurchaseOrder;
import com.hand.demo.domain.repository.PurchaseOrderRepository;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * (PurchaseOrder)表控制层
 *
 * @author
 * @since 2024-11-01 16:59:21
 */

@RestController("purchaseOrderController.v1")
@RequestMapping("/v1/{organizationId}/purchase-orders")
public class PurchaseOrderController extends BaseController {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @ApiOperation(value = "列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<Page<PurchaseOrder>> list(PurchaseOrder purchaseOrder, @PathVariable Long organizationId,
                                                    @ApiIgnore @SortDefault(value = PurchaseOrder.FIELD_ID,
                                                            direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<PurchaseOrder> list = purchaseOrderService.selectList(pageRequest, purchaseOrder);
        return Results.success(list);
    }

    @ApiOperation(value = "list")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/list")
    public ResponseEntity<List<PurchaseOrderDTO>> listOrders(PurchaseOrder purchaseOrder, @PathVariable Long organizationId) {
        return Results.success(purchaseOrderService.getListPurchaseOrder(purchaseOrder));
    }

    @ApiOperation(value = "明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{id}/detail")
    public ResponseEntity<PurchaseOrderDTO> detail(@PathVariable Long id) {
        PurchaseOrderDTO resultDto = purchaseOrderService.getPurchaseOrderDetail(id);
        return Results.success(resultDto);
    }

    @ApiOperation(value = "创建或更新")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<PurchaseOrder>> save(@PathVariable Long organizationId, @RequestBody List<PurchaseOrder> purchaseOrders) {
        validObject(purchaseOrders);
        SecurityTokenHelper.validTokenIgnoreInsert(purchaseOrders);
        purchaseOrders.forEach(item -> item.setTenantId(organizationId));
        purchaseOrderService.saveData(purchaseOrders);
        return Results.success(purchaseOrders);
    }

    @ApiOperation(value = "删除")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<PurchaseOrder> purchaseOrders) {
        SecurityTokenHelper.validToken(purchaseOrders);
        purchaseOrderRepository.batchDeleteByPrimaryKey(purchaseOrders);
        return Results.success();
    }

}

