package com.hand.demo.app.service.impl;

import com.hand.demo.infra.constant.TaskConstant;
import com.hand.demo.infra.feign.TaskFeign;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import com.hand.demo.app.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.repository.TaskRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务表(Task)应用服务
 *
 * @author Allan
 * @since 2024-10-28 14:57:35
 */
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CodeRuleBuilder codeRuleBuilder;
    private final LovAdapter lovAdapter;
    private final TaskFeign taskFeign;

    public TaskServiceImpl(CodeRuleBuilder codeRuleBuilder, TaskRepository taskRepository, LovAdapter lovAdapter, TaskFeign taskFeign) {
        this.codeRuleBuilder = codeRuleBuilder;
        this.taskRepository = taskRepository;
        this.lovAdapter = lovAdapter;
        this.taskFeign = taskFeign;
    }

    @Override
    public Page<Task> selectList(PageRequest pageRequest, Task task) {
        return PageHelper.doPageAndSort(pageRequest, () -> taskRepository.selectList(task));
    }

    @Override
    public List<Task> saveData(List<Task> tasks) {

        List<LovValueDTO> countStatusList = lovAdapter.queryLovValue(TaskConstant.LOV_CODE, BaseConstants.DEFAULT_TENANT_ID);
        List<String> validTaskTypes = countStatusList.stream().map(LovValueDTO::getValue).collect(Collectors.toList());

        List<String> errorList = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            String taskType = task.getTaskType();
            if (!validTaskTypes.contains(taskType)) {
                String errorMsg = "Row " + (i+1) + " of task type " + taskType + " is not a valid Task Type";
                errorList.add(errorMsg);
            }
        }

        // return list of errors
        if (!errorList.isEmpty()) {
            throw new CommonException(String.valueOf(errorList));
        }

        for (Task task: tasks) {
            // give new task number to a new task
            // update task don't need to change their task number
            if (task.getId() == null) {
                String taskNumber = createTaskWithCodeRule();
                task.setTaskNumber(taskNumber);
            }
        }

        List<Task> insertList = tasks.stream().filter(line -> line.getId() == null).collect(Collectors.toList());
        List<Task> updateList = tasks.stream().filter(line -> line.getId() != null).collect(Collectors.toList());

        taskRepository.batchInsertSelective(insertList);
        taskRepository.batchUpdateByPrimaryKeySelective(updateList);

        return tasks;
    }

    @Override
    public String createTaskWithCodeRule() {
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        String realName = userDetails.getRealName();
        Map<String, String> variableMap = new HashMap<>();
        String realNameFormat = "-" + realName + "-";
        variableMap.put("customSegment", realNameFormat);

        return codeRuleBuilder.generateCode(TaskConstant.RULE_CODE, variableMap);
    }

    @Override
    public List<Object> findTaskDetailFeign(Task task, Long organizationId, PageRequest pageRequest) {
        ResponseEntity<List<Object>> feignResponse = taskFeign.onlineUserList();
        return feignResponse.getBody();
    }
}

