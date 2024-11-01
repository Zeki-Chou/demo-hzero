package com.hand.demo.app.service.impl;

import com.hand.demo.infra.constant.TaskConstant;
import io.choerodon.core.domain.Page;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.message.util.DateUtils;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
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
 * @author fatih khoiri
 * @since 2024-10-28 14:39:19
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

//    @Override
//    public void saveData(List<Task> tasks) {
//
//        List<LovValueDTO> validTaskTypesList = lovAdapter.queryLovValue(TaskConstant.SET_CODE, Long.valueOf(TaskConstant.TENANT_ID));
//        List<String> validTaskTypes = validTaskTypesList.stream()
//                .map(LovValueDTO::getValue)
//                .collect(Collectors.toList());
//
//        tasks.stream()
//                .filter(task -> !validTaskTypes.contains(task.getTaskType()))
//                .findFirst()
//                .ifPresent(invalidTask -> {
//                    throw new IllegalArgumentException("Error: Invalid task type '" + invalidTask.getTaskType() +
//                            "'. Allowed values are: " + validTaskTypes);
//                });
//
//        Map<String, String> variableMap = new HashMap<>();
//
//
//        variableMap.put("customSegment", ((" - "+DetailsHelper.getUserDetails().getRealName())+" - "));
//
//        String batchCode = codeRuleBuilder.generateCode(TaskConstant.RULE_CODE, variableMap);
//
//        List<Task> insertList = tasks.stream()
//                .filter(task -> task.getId() == null)
//                .filter(task -> validTaskTypes.contains(task.getTaskType()))
//                .collect(Collectors.toList());
//
//        List<Task> updateList = tasks.stream()
//                .filter(task -> task.getId() != null)
//                .filter(task -> validTaskTypes.contains(task.getTaskType()))
//                .collect(Collectors.toList());
//
//        insertList.forEach(task -> task.setTaskNumber(batchCode));
//
//        taskRepository.batchInsertSelective(insertList);
//        taskRepository.batchUpdateByPrimaryKeySelective(updateList);
//    }

    @Override
    public void saveData(List<Task> tasks) {
        List<LovValueDTO> validTaskTypesList = lovAdapter.queryLovValue(TaskConstant.SET_CODE, Long.valueOf(TaskConstant.TENANT_ID));
        List<String> validTaskTypes = validTaskTypesList.stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        StringBuilder errorMessages = new StringBuilder();
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (!validTaskTypes.contains(task.getTaskType())) {
                errorMessages.append("line num: ").append(i+1)
                        .append(", Task Description: ").append(task.getTaskDescription())
                        .append(", TaskType: ").append(task.getTaskType())
                        .append(", ");
            }
        }

        if (errorMessages.length() > 0) {
            throw new IllegalArgumentException("Error: Allowed values are: " + validTaskTypes + ". " +
                    "Invalid tasks:\n" + errorMessages.toString());
        }


        Map<String, String> variableMap = new HashMap<>();
        variableMap.put("customSegment", "-" + DetailsHelper.getUserDetails().getRealName() + "-");

        List<Task> insertList = tasks.stream()
                .filter(task -> task.getId() == null)
                .collect(Collectors.toList());

        List<Task> updateList = tasks.stream()
                .filter(task -> task.getId() != null)
                .collect(Collectors.toList());

        List<String> batchCodes = codeRuleBuilder.generateCode(insertList.size(), TaskConstant.RULE_CODE, variableMap);

        for (int i = 0; i < insertList.size(); i++) {
            Task task = insertList.get(i);
            task.setTaskNumber(batchCodes.get(i));
        }

        taskRepository.batchInsertSelective(insertList);
        taskRepository.batchUpdateByPrimaryKeySelective(updateList);

    }
}

