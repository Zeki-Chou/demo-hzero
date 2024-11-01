package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.OrderHeaderDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.core.base.BaseController;
import org.hzero.core.cache.ProcessCacheValue;
import org.hzero.core.util.Results;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hand.demo.app.service.OrderHeader47355Service;
import com.hand.demo.domain.entity.OrderHeader47355;
import com.hand.demo.domain.repository.OrderHeader47355Repository;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * (OrderHeader47355)表控制层
 *
 * @author
 * @since 2024-11-01 12:27:37
 */

@RestController("orderHeader47355Controller.v1")
@RequestMapping("/v1/{organizationId}/order-header47355s")
public class OrderHeader47355Controller extends BaseController {

    @Autowired
    private OrderHeader47355Repository orderHeader47355Repository;

    @Autowired
    private OrderHeader47355Service orderHeader47355Service;

    @ApiOperation(value = "列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<Page<OrderHeader47355>> list(OrderHeader47355 orderHeader47355, @PathVariable Long organizationId,
                                                       @ApiIgnore @SortDefault(value = OrderHeader47355.FIELD_ID,
                                                               direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<OrderHeader47355> list = orderHeader47355Service.selectList(pageRequest, orderHeader47355);
        return Results.success(list);
    }

    @ApiOperation(value = "明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{id}/detail")
    public ResponseEntity<OrderHeader47355> detail(@PathVariable Long id) {
        OrderHeader47355 orderHeader47355 = orderHeader47355Repository.selectByPrimary(id);
        return Results.success(orderHeader47355);
    }

    @ApiOperation(value = "创建或更新")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<OrderHeader47355>> save(@PathVariable Long organizationId, @RequestBody List<OrderHeader47355> orderHeader47355s) {
        validObject(orderHeader47355s);
        SecurityTokenHelper.validTokenIgnoreInsert(orderHeader47355s);
        orderHeader47355s.forEach(item -> item.setTenantId(organizationId));
        orderHeader47355Service.saveData(orderHeader47355s);
        return Results.success(orderHeader47355s);
    }

    @ApiOperation(value = "删除")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<OrderHeader47355> orderHeader47355s) {
        SecurityTokenHelper.validToken(orderHeader47355s);
        orderHeader47355Repository.batchDeleteByPrimaryKey(orderHeader47355s);
        return Results.success();
    }

    @ApiOperation(value = "Get orders")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/order")
    @ProcessCacheValue
    public List<OrderHeaderDTO> getDataFromCache(@PathVariable Long organizationId, OrderHeader47355 orderHeader47355) {
        return orderHeader47355Service.getOrders(orderHeader47355);
    }

}

