package com.hand.demo.api.controller.v1;

import com.hand.demo.api.controller.dto.UserTaskInfoDTO;
import com.hand.demo.app.service.UserTaskService;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userTaskController.v1" )
@RequestMapping("/v1/{organizationId}/userTasks" )
public class UserTaskController extends BaseController {

    private final UserTaskService service;

    @Autowired
    public UserTaskController(UserTaskService service) {
        this.service = service;
    }

    @ApiOperation("get user task info")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{id}")
    public ResponseEntity<UserTaskInfoDTO> detail(@PathVariable("organizationId") Long organizationId, @PathVariable("id") Long id) {
        UserTaskInfoDTO info = service.findUserTaskInfo(id);
        return Results.success(info);
    }

    @ApiOperation("get list task info")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/tasks")
    public ResponseEntity<List<UserTaskInfoDTO>> list(@PathVariable("organizationId") Long organizationId, UserTaskInfoDTO dto) {
        List<UserTaskInfoDTO> taskInfos = service.findList(dto);
        return Results.success(taskInfos);
    }
}
