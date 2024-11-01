package com.hand.demo.api.controller.v1;

import com.hand.demo.domain.dto.DesensitizedUserDTO;
import com.hand.demo.domain.dto.ExternalInterfaceDTO;
import com.hand.demo.domain.dto.UserDTO;
import com.hand.demo.domain.dto.UserTaskDTO;
import com.hand.demo.domain.entity.Task;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;
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
 * @author azhar.naufal@hand-global.com 2024-10-17 13:48:26
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

    @ApiOperation(value = "getEmployeeWithTask")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/getEmployeeWithTask")
    public ResponseEntity<Page<UserTaskDTO>> getTaskByEmployee(@PathVariable("organizationId") Long organizationId, @ApiIgnore @SortDefault(value = Task.FIELD_ID,
            direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<UserTaskDTO> list = userService.usersWithTask(organizationId, pageRequest);
        return Results.success(list);
    }

    @ApiOperation(value = "userTaskByEmpOrTask")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/userTaskByEmpOrTask")
    public ResponseEntity<List<UserTaskDTO>> getUserTaskByEmpOrTask(@PathVariable("organizationId") Long organizationId, UserTaskDTO userTaskDTO) {
        List<UserTaskDTO> userTaskDTOList = userService.userWithTask(organizationId, userTaskDTO);
        return Results.success(userTaskDTOList);
    }

    @ApiOperation(value = "User Save")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/save")
    public ResponseEntity<List<DesensitizedUserDTO>> save(@PathVariable Long organizationId, @RequestBody List<User> users) {
        validObject(users);
        SecurityTokenHelper.validTokenIgnoreInsert(users);
//        users.forEach(item -> item.setTenantId(organizationId));
        List<DesensitizedUserDTO> desensitizedUserDTOS = userService.saveDataMasking(users);
        return Results.success(desensitizedUserDTOS);
    }

    @ApiOperation(value = "external save")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/save-external")
    public ResponsePayloadDTO externalCalculator(ExternalInterfaceDTO externalInterfaceDTO,
                                                 @RequestBody List<UserDTO> listJsonUser) {
        return userService.invokeSaveUSer(externalInterfaceDTO, listJsonUser);
    }
}
