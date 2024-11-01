package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.IamUserDTO;
import com.hand.demo.api.dto.OrderHeader47360DTO;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hand.demo.app.service.OrderHeader47360Service;
import com.hand.demo.domain.entity.OrderHeader47360;
import com.hand.demo.domain.repository.OrderHeader47360Repository;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * Order Header
 *
 * @author
 * @since 2024-11-01 10:48:42
 */

@RestController("orderHeader47360Controller.v1")
@RequestMapping("/v1/{organizationId}/order-header47360s")
public class OrderHeader47360Controller extends BaseController {

    @Autowired
    private OrderHeader47360Repository orderHeader47360Repository;

    @Autowired
    private OrderHeader47360Service orderHeader47360Service;

    @ApiOperation(value = "Page")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<Page<OrderHeader47360DTO>> page(OrderHeader47360 orderHeader47360, @PathVariable Long organizationId,
                                                          @ApiIgnore @SortDefault(value = OrderHeader47360.FIELD_ID,
                                                               direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<OrderHeader47360DTO> page = orderHeader47360Service.selectList(pageRequest, orderHeader47360);
        return Results.success(page);
    }

    @ApiOperation(value = "Detail")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/detail")
    public ResponseEntity<OrderHeader47360DTO> detail(@PathVariable Long organizationId,@RequestParam Long id) {
        OrderHeader47360DTO orderHeader47360DTO = orderHeader47360Service.detail(organizationId,id);
        return Results.success(orderHeader47360DTO);
    }

    @ApiOperation(value = "Save")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<OrderHeader47360DTO>> save(@PathVariable Long organizationId, @RequestBody List<OrderHeader47360> orderHeader47360s) {
        validObject(orderHeader47360s);
        SecurityTokenHelper.validTokenIgnoreInsert(orderHeader47360s);
        orderHeader47360s.forEach(item -> item.setTenantId(organizationId));
        List<OrderHeader47360DTO> orderHeader47360DTOS = orderHeader47360Service.saveData(orderHeader47360s);
        return Results.success(orderHeader47360DTOS);
    }

    @ApiOperation(value = "Delete")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<OrderHeader47360> orderHeader47360s) {
        SecurityTokenHelper.validToken(orderHeader47360s);
        orderHeader47360Repository.batchDeleteByPrimaryKey(orderHeader47360s);
        return Results.success();
    }

//    @ApiOperation(value = "Test Cache")
//    @Permission(level = ResourceLevel.ORGANIZATION)
//    @GetMapping("test-cache")
//    @ProcessCacheValue
//    public ResponseEntity<IamUserDTO> TestCache(@PathVariable Long organizationId) {
//        IamUserDTO iamUserDTO = new IamUserDTO();
//        iamUserDTO.setId(2L);
//        return Results.success(iamUserDTO);
//    }
}

