package com.hand.demo.api.controller.v1;

import com.hand.demo.infra.feign.HPFMFeign;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hand.demo.app.service.TaskService;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.repository.TaskRepository;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * 任务表(Task)表控制层
 *
 * @author
 * @since 2024-10-28 14:39:57
 */

@RestController("taskController.v1")
@RequestMapping("/v1/{organizationId}/tasks")
@AllArgsConstructor
public class TaskController extends BaseController {

    private TaskRepository taskRepository;

    private TaskService taskService;

    private HPFMFeign hpfmFeign;

    @ApiOperation(value = "任务表列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<Page<Task>> list(Task task, @PathVariable Long organizationId,
                                           @ApiIgnore @SortDefault(value = Task.FIELD_ID,
                                                   direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<Task> list = taskService.selectList(pageRequest, task);
        return Results.success(list);
//        return Results.success(internalTaskService.list(organizationId));

    }

    @ApiOperation(value = "任务表明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{id}/detail")
    public ResponseEntity<Task> detail(@PathVariable Long id) {
        Task task = taskRepository.selectByPrimary(id);
        return Results.success(task);
    }

    @ApiOperation(value = "Save and update task")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<Task>> save(@PathVariable Long organizationId, @RequestBody List<Task> tasks) {
        validObject(tasks);
        SecurityTokenHelper.validTokenIgnoreInsert(tasks);
        tasks.forEach(item -> item.setTenantId(organizationId));
        taskService.saveData(tasks);
        return Results.success(tasks);
    }

    @ApiOperation(value = "删除任务表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<Task> tasks) {
        SecurityTokenHelper.validToken(tasks);
        taskRepository.batchDeleteByPrimaryKey(tasks);
        return Results.success();
    }

    @ApiOperation(value = "List online users")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/internal")
    public ResponseEntity<List<Object>> listOnlineUsers(@PathVariable("organizationId") Long organizationId) {
        return hpfmFeign.onlineUserList();
    }

}

