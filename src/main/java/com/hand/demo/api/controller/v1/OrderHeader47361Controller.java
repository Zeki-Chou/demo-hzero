package com.hand.demo.api.controller.v1;

import com.hand.demo.domain.dto.IamUserDTO;
import com.hand.demo.domain.dto.OrderHeaderDTO;
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
import org.opensaml.saml2.metadata.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hand.demo.app.service.OrderHeader47361Service;
import com.hand.demo.domain.entity.OrderHeader47361;
import com.hand.demo.domain.repository.OrderHeader47361Repository;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * Order Header Table(OrderHeader47361)表控制层
 *
 * @author
 * @since 2024-11-01 10:52:09
 */

@RestController("orderHeader47361Controller.v1")
@RequestMapping("/v1/{organizationId}/order-header47361s")
public class OrderHeader47361Controller extends BaseController {

    @Autowired
    private OrderHeader47361Repository orderHeader47361Repository;

    @Autowired
    private OrderHeader47361Service orderHeader47361Service;

    @ApiOperation(value = "Order Header Table列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<Page<OrderHeader47361>> list(OrderHeader47361 orderHeader47361, @PathVariable Long organizationId,
                                                       @ApiIgnore @SortDefault(value = OrderHeader47361.FIELD_ORDER_ID,
                                                               direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<OrderHeader47361> list = orderHeader47361Service.selectList(pageRequest, orderHeader47361);
        return Results.success(list);
    }

    @ApiOperation(value = "Order Header Table明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{orderId}/detail")
    public ResponseEntity<OrderHeader47361> detail(@PathVariable Long orderId) {
        OrderHeader47361 orderHeader47361 = orderHeader47361Repository.selectByPrimary(orderId);
        return Results.success(orderHeader47361);
    }

    @ApiOperation(value = "创建或更新Order Header Table")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<OrderHeader47361>> save(@PathVariable Long organizationId, @RequestBody List<OrderHeader47361> orderHeader47361s) {
        validObject(orderHeader47361s);
        SecurityTokenHelper.validTokenIgnoreInsert(orderHeader47361s);
        orderHeader47361s.forEach(item -> item.setTenantId(organizationId));
        orderHeader47361Service.saveData(orderHeader47361s);
        return Results.success(orderHeader47361s);
    }

    @ApiOperation(value = "删除Order Header Table")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<OrderHeader47361> orderHeader47361s) {
        SecurityTokenHelper.validToken(orderHeader47361s);
        orderHeader47361Repository.batchDeleteByPrimaryKey(orderHeader47361s);
        return Results.success();
    }

    @ApiOperation(value = "List Detail")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/list-detail")
    @ProcessCacheValue
    public ResponseEntity<List<OrderHeaderDTO>> listDetail(OrderHeader47361 orderHeader47361, @PathVariable Long organizationId) {
        List<OrderHeaderDTO> list = orderHeader47361Service.selectListDetail(orderHeader47361);
        return Results.success(list);
    }

}

