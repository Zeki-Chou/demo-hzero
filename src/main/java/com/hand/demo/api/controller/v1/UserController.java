package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.UserDTO;
import com.hand.demo.api.dto.UserResponseDTO;
import com.hand.demo.domain.entity.Task;
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
 * @author fatih.khoiri@hand-global.com 2024-10-17 13:57:07
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

    @ApiOperation(value = "TEST LIST")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/userList")
    public ResponseEntity<List<UserDTO>> listTask(@PathVariable("organizationId") Long organizationId, UserDTO userDTO) {
        List<UserDTO> list = userService.listTask(organizationId, userDTO);
        return Results.success(list);
    }

//    @ApiOperation(value = "TEST LIST")
//    @Permission(level = ResourceLevel.ORGANIZATION)
//    @GetMapping("/userList")
//    public ResponseEntity<List<UserDTO>> listTask(@PathVariable("organizationId") Long organizationId, UserDTO userDTO) {
//        List<UserDTO> list = userService.listTask(organizationId, userDTO);
//        return Results.success(list);
//    }

    @ApiOperation(value = "Save")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/save")
    public ResponseEntity<List<UserResponseDTO>> save(@PathVariable Long organizationId, @RequestBody List<User> users) {
        validObject(users);
//        SecurityTokenHelper.validTokenIgnoreInsert(users);
//        users.forEach(item -> item.setTenantId(organizationId));
//        userService.saveData(users);
        List<UserResponseDTO> userResponseDTOS = userService.saveData(users);
        return Results.success(userResponseDTOS);
    }
}
