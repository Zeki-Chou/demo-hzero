package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.UserCacheDTO;
import com.hand.demo.app.service.OrderHeader47356Service;
import com.hand.demo.domain.entity.OrderHeader47356;
import com.hand.demo.domain.repository.OrderHeader47356Repository;
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
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * (OrderHeader47356)表控制层
 *
 * @author
 * @since 2024-11-01 13:17:15
 */

@RestController("orderHeader47356Controller.v1")
@RequestMapping("/v1/{organizationId}/order-header47356s")
public class OrderHeader47356Controller extends BaseController {

    @Autowired
    private OrderHeader47356Repository orderHeader47356Repository;

    @Autowired
    private OrderHeader47356Service orderHeader47356Service;

    @ApiOperation(value = "List Order")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<Page<OrderHeader47356>> list(OrderHeader47356 orderHeader47356, @PathVariable Long organizationId,
                                                       @ApiIgnore @SortDefault(value = OrderHeader47356.FIELD_ID,
                                                               direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<OrderHeader47356> list = orderHeader47356Service.selectList(pageRequest, orderHeader47356);
        return Results.success(list);
    }

    @ApiOperation(value = "Detail Order By Id")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{id}/detail")
    public ResponseEntity<UserCacheDTO> detail(@PathVariable Long organizationId, @PathVariable Long id) {
        UserCacheDTO cache = orderHeader47356Service.getOrderById(organizationId, id);
        return Results.success(cache);
    }

    @ApiOperation(value = "Save Order")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<OrderHeader47356>> save(@PathVariable Long organizationId, @RequestBody List<OrderHeader47356> orderHeader47356s) {
        validObject(orderHeader47356s);
//        SecurityTokenHelper.validTokenIgnoreInsert(orderHeader47356s);
        orderHeader47356s.forEach(item -> item.setTenantId(organizationId));
        orderHeader47356Service.saveData(orderHeader47356s);
        return Results.success(orderHeader47356s);
    }

    @ApiOperation(value = "Remove Order")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<OrderHeader47356> orderHeader47356s) {
        SecurityTokenHelper.validToken(orderHeader47356s);
        orderHeader47356Repository.batchDeleteByPrimaryKey(orderHeader47356s);
        return Results.success();
    }
}

