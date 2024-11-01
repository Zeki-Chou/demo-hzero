package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.OrderHeaderDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hzero.core.base.BaseController;
import org.hzero.core.cache.ProcessCacheValue;
import org.hzero.core.util.Results;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hand.demo.app.service.OrderHeader47358Service;
import com.hand.demo.domain.entity.OrderHeader47358;
import com.hand.demo.domain.repository.OrderHeader47358Repository;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * (OrderHeader47358)表控制层
 *
 * @author
 * @since 2024-11-01 10:52:45
 */

@RestController("orderHeader47358Controller.v1")
@RequestMapping("/v1/{organizationId}/order-header47358s")
@Slf4j
public class OrderHeader47358Controller extends BaseController {

    @Autowired
    private OrderHeader47358Repository orderHeader47358Repository;

    @Autowired
    private OrderHeader47358Service orderHeader47358Service;

    @ApiOperation(value = "列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<Page<OrderHeaderDTO>> list(OrderHeader47358 orderHeader47358, @PathVariable Long organizationId,
                                                     @ApiIgnore @SortDefault(value = OrderHeader47358.FIELD_ID,
                                                               direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<OrderHeaderDTO> list = orderHeader47358Service.selectList(pageRequest, orderHeader47358);
        return Results.success(list);
    }

    @ApiOperation(value = "Get List Cached")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/get-list")
    public ResponseEntity<List<OrderHeaderDTO>> getList(@PathVariable Long organizationId) {
        List<OrderHeaderDTO> list = orderHeader47358Service.getList();
        return Results.success(list);
    }

    @ApiOperation(value = "明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{id}/detail")
    public ResponseEntity<OrderHeaderDTO> detail(@PathVariable Long id) {
//        OrderHeader47358 orderHeader47358 = orderHeader47358Repository.selectByPrimary(id);
        OrderHeaderDTO orderHeaderDTO = orderHeader47358Service.getDetail(id);
        return Results.success(orderHeaderDTO);
    }

    @ApiOperation(value = "创建或更新")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<OrderHeader47358>> save(@PathVariable Long organizationId, @RequestBody List<OrderHeader47358> orderHeader47358s) {
        validObject(orderHeader47358s);
//        SecurityTokenHelper.validTokenIgnoreInsert(orderHeader47358s);
        orderHeader47358s.forEach(item -> item.setTenantId(organizationId));
        orderHeader47358Service.saveData(orderHeader47358s);
        return Results.success(orderHeader47358s);
    }

    @ApiOperation(value = "删除")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<OrderHeader47358> orderHeader47358s) {
        SecurityTokenHelper.validToken(orderHeader47358s);
        orderHeader47358Repository.batchDeleteByPrimaryKey(orderHeader47358s);
        return Results.success();
    }

}

