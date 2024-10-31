package com.hand.demo.api.controller.v1;

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
import com.hand.demo.app.service.TaskService;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.repository.TaskRepository;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * 任务表(Task)表控制层
 *
 * @author
 * @since 2024-10-31 16:44:57
 */

@RestController("taskController.v1")
@RequestMapping("/v1/{organizationId}/tasks")
public class TaskController extends BaseController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskService taskService;

    @ApiOperation(value = "任务表列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<Page<Task>> list(Task task, @PathVariable Long organizationId,
                                           @ApiIgnore @SortDefault(value = Task.FIELD_${pk.obj.name.toUpperCase()},
                                                   direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<Task> list = taskService.selectList(pageRequest, task);
        return Results.success(list);
    }

    @ApiOperation(value = "任务表明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{${pk.name}}/detail")
    public ResponseEntity<Task> detail(@PathVariable Long $ {
        pk.name
    })

    {
        Task task = taskRepository.selectByPrimary($ {
        pk.name
    })
        return Results.success(task);
    }

    @ApiOperation(value = "创建或更新任务表")
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

}

