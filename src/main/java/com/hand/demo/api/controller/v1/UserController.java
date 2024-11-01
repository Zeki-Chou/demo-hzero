package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.UserTasksDTO;
import com.hand.demo.api.dto.UserTasksRequest;
import org.hzero.core.util.Results;
import org.hzero.core.base.BaseController;
import com.hand.demo.app.service.UserService;
import com.hand.demo.domain.entity.User;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;

import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * 用户表 管理 API
 *
 * @author joseph.julio@hand-global.com 2024-10-17 13:56:56
 */
@RestController("userController.v1" )
@RequestMapping("/v1/{organizationId}/users" )
public class UserController extends BaseController {

    private final UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ApiOperation(value = "用户表列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<Page<User>> list(@PathVariable("organizationId") Long organizationId, User user, @ApiIgnore @SortDefault(value = User.FIELD_ID,
            direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<User> list = userService.list(organizationId, user, pageRequest);
        return Results.success(list);
    }

//    @ApiOperation(value = "get-user-with-tasks")
//    @Permission(level = ResourceLevel.ORGANIZATION)
//    @GetMapping("/with-tasks")
//    public ResponseEntity<List<UserTasksDTO>> getUserWithTasks(@PathVariable("organizationId") Long organizationId, User user, @ApiIgnore @SortDefault(value = User.FIELD_ID,
//            direction = Sort.Direction.DESC) PageRequest pageRequest) {
//        List<UserTasksDTO> list = userService.getUserWithTasks(organizationId, user, pageRequest);
//        return Results.success(list);
//    }

    @ApiOperation(value = "get-user-with-tasks")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/with-tasks")
    public ResponseEntity<List<UserTasksDTO>> getUserWithTasks(
            @PathVariable("organizationId") Long organizationId,
            UserTasksDTO userTasksDTO
            ) {
        List<UserTasksDTO> list = userService.getUsersWithTasks(userTasksDTO);
        return Results.success(list);
    }

    @ApiOperation(value = "用户表明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{id}")
    public ResponseEntity<User> detail(@PathVariable("organizationId") Long organizationId, @PathVariable Long id) {
        User user =userService.detail(organizationId, id);
        return Results.success(user);
    }

    @ApiOperation(value = "创建用户表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<User> create(@PathVariable("organizationId") Long organizationId, @RequestBody User user) {
            userService.create(organizationId, user);
        return Results.success(user);
    }

    @ApiOperation(value = "修改用户表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping
    public ResponseEntity<User> update(@PathVariable("organizationId") Long organizationId, @RequestBody User user) {
        userService.update(organizationId, user);
        return Results.success(user);
    }

    @ApiOperation(value = "删除用户表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody User user) {
        userService.remove(user);
        return Results.success();
    }

    @ApiOperation(value = "Save User Table")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/save")
    public ResponseEntity<List<User>> save(@PathVariable Long organizationId, @RequestBody List<User> users) {
        validObject(users);
        SecurityTokenHelper.validTokenIgnoreInsert(users);
//        users.forEach(item -> item.setTenantId(organizationId));

        return Results.success(userService.saveData(users));
    }

}
