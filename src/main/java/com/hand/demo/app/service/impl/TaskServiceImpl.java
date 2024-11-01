package com.hand.demo.app.service.impl;

import com.hand.demo.infra.constant.TaskConstants;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.ext.IllegalArgumentException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import lombok.AllArgsConstructor;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import com.hand.demo.app.service.TaskService;
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
 * @author
 * @since 2024-10-28 14:39:56
 */
@Service
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {
    private TaskRepository taskRepository;

    private final CodeRuleBuilder codeRuleBuilder;

    private final LovAdapter lovAdapter;

    @Override
    public Page<Task> selectList(PageRequest pageRequest, Task task) {
        return PageHelper.doPageAndSort(pageRequest, () -> taskRepository.selectList(task));
    }

    @Override
    public void saveData(List<Task> tasks) {
        Map<String, String> variableMap = new HashMap<>();
        variableMap.put("customSegment", "-" + DetailsHelper.getUserDetails().getRealName() + "-");

        List<String> errors = new ArrayList<>();
        List<String> allowedTaskTypes = lovAdapter.queryLovValue(TaskConstants.LOV_CODE, BaseConstants.DEFAULT_TENANT_ID)
                .stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        List<Task> insertList = tasks.stream()
                .filter(line -> line.getId() == null)
                .peek(task -> {
                    if (!allowedTaskTypes.contains(task.getTaskType())) {
                        errors.add("Task type " + task.getTaskType() + " not available");
                    }
                })
                .collect(Collectors.toList());

        List<Task> updateList = tasks.stream()
                .filter(line -> line.getId() != null)
                .peek(task -> {
                    if (!allowedTaskTypes.contains(task.getTaskType())) {
                        errors.add("Task type " + task.getTaskType() + " not available");
                    }
                })
                .collect(Collectors.toList());

        if(!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.toString());
        }

        List<String> batchCode = codeRuleBuilder.generateCode(insertList.size(), TaskConstants.CODE_RULE, variableMap);

        for(int i = 0; i < insertList.size(); i++) {
            Task task = insertList.get(i);
            task.setTaskNumber(batchCode.get(i));
        }

        taskRepository.batchInsertSelective(insertList);
        taskRepository.batchUpdateByPrimaryKeySelective(updateList);
    }
}

