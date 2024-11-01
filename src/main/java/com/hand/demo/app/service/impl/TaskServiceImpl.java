package com.hand.demo.app.service.impl;

import com.hand.demo.infra.constant.CodeRuleConstant;
import com.hand.demo.infra.constant.WinvCountConstant;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.checkerframework.checker.units.qual.A;
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
 * @since 2024-10-28 16:11:57
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
        Map<String, String> variableMap = new HashMap<>();
        String realName = DetailsHelper.getUserDetails().getRealName();
        variableMap.put("customSegment", ("-" + realName + "-"));

//        String batchCode = codeRuleBuilder.generateCode(CodeRuleConstant.CODE_RULE, variableMap);

        List<String> validTaskTypes = lovAdapter
                .queryLovValue(WinvCountConstant.LovCode.TASK_TYPE, BaseConstants.DEFAULT_TENANT_ID)
                .stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());

        List<Task> insertList = new LinkedList<>();
        List<Task> updateList = new LinkedList<>();
        List<String> errorMessages = new ArrayList<>();

//        List<Task> insertList = tasks.stream().filter(line -> line.getId() == null).collect(Collectors.toList());
//        List<Task> updateList = tasks.stream().filter(line -> line.getId() != null).collect(Collectors.toList());

        int lineNumber = 1;

        for (Task task : tasks) {
            if (!validTaskTypes.contains(task.getTaskType())) {
                errorMessages.add("Line Number: " + lineNumber + ", is invalid task type: " + task.getTaskType());
            }
            if (task.getId() == null) {
                insertList.add(task);
            } else {
                updateList.add(task);
            }
            lineNumber++;
        }

        if (!errorMessages.isEmpty()) {
            throw new CommonException(String.join(" | ", errorMessages));
        }

        for (Task task : insertList) {
            String batchCode = codeRuleBuilder.generateCode(CodeRuleConstant.CODE_RULE, variableMap);
            task.setTaskNumber(batchCode);
        }

        taskRepository.batchInsertSelective(insertList);
        taskRepository.batchUpdateByPrimaryKeySelective(updateList);
    }
}

