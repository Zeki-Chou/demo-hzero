package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.ImUserDto;
import com.hand.demo.api.dto.OrderResponseDto;
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
import com.hand.demo.app.service.Order47357Service;
import com.hand.demo.domain.entity.Order47357;
import com.hand.demo.domain.repository.Order47357Repository;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * (Order47357)表控制层
 *
 * @author
 * @since 2024-11-01 10:46:44
 */

@RestController("order47357Controller.v1")
@RequestMapping("/v1/{organizationId}/order47357s")
public class Order47357Controller extends BaseController {

    @Autowired
    private Order47357Repository order47357Repository;

    @Autowired
    private Order47357Service order47357Service;

    @ApiOperation(value = "Get Orders")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("list")
    @ProcessCacheValue
    public ResponseEntity<List<OrderResponseDto>> list(Order47357 order47357, @PathVariable Long organizationId) {
        List<OrderResponseDto> list = order47357Service.selectList(order47357);
        return Results.success(list);
    }


    @ApiOperation(value = "明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{id}/detail")
    public ResponseEntity<Order47357> detail(@PathVariable Long id) {
        Order47357 order47357 = order47357Repository.selectByPrimary(id);
        return Results.success(order47357);
    }

    @ApiOperation(value = "Save orders")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("save")
    @ProcessCacheValue
    public ResponseEntity<List<OrderResponseDto>> save(@RequestBody List<Order47357> order47357s) {
        List<OrderResponseDto> response = order47357Service.saveData(order47357s);
        return Results.success(response);
    }


    @ApiOperation(value = "删除")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<Order47357> order47357s) {
        SecurityTokenHelper.validToken(order47357s);
        order47357Repository.batchDeleteByPrimaryKey(order47357s);
        return Results.success();
    }



}

