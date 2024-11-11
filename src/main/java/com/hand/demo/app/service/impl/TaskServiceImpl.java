package com.hand.demo.app.service.impl;

import com.hand.demo.infra.constant.TaskConstants;
import com.netflix.discovery.converters.Auto;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.message.util.DateUtils;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.TaskService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.repository.TaskRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务表(Task)应用服务
 *
 * @author
 * @since 2024-10-28 14:36:17
 */
@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CodeRuleBuilder codeRuleBuilder;

    @Autowired
    private LovAdapter lovAdapter;

    @Override
    public Page<Task> selectList(PageRequest pageRequest, Task task) {
        return PageHelper.doPageAndSort(pageRequest, () -> taskRepository.selectList(task));
    }

    @Override
    public void saveData(List<Task> tasks) {
        List<String> validTaskTypes = lovAdapter
                .queryLovValue(TaskConstants.LovCode.TASK_TYPE, BaseConstants.DEFAULT_TENANT_ID)
                .stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        List<String> invalidTasks = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.getId() == null && !validTaskTypes.contains(task.getTaskType())) {
                invalidTasks.add("Line No" + (i + 1) + " - Task Type: " + task.getTaskType() + " is invalid.");
            }
        }

        if (!invalidTasks.isEmpty()) {
            throw new CommonException("Invalid task types found: " + String.join(", ", invalidTasks));
        }

        List<Task> insertList = tasks.stream()
                .filter(task -> task.getId() == null)
                .collect(Collectors.toList());

        List<Task> updateList = tasks.stream()
                .filter(task -> task.getId() != null)
                .collect(Collectors.toList());

        insertList.forEach(task -> {
            Map<String, String> variableMap = new HashMap<>();
            variableMap.put("customSegment", ("-" + DetailsHelper.getUserDetails().getRealName()) + "-");
            String uniqueCode = codeRuleBuilder.generateCode(TaskConstants.CODE_RULE, variableMap);
            task.setTaskNumber(uniqueCode);
        });

        taskRepository.batchInsertSelective(insertList);
        taskRepository.batchUpdateByPrimaryKeySelective(updateList);
    }


}

