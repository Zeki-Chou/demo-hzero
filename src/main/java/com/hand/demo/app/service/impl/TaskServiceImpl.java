package com.hand.demo.app.service.impl;

import com.hand.demo.infra.constant.Constants;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @since 2024-10-28 17:05:28
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
    public void saveData(Long organizationId, List<Task> tasks) {
        List<LovValueDTO> taskTypeDTOs = lovAdapter.queryLovValue(Constants.LOV_CODE, BaseConstants.DEFAULT_TENANT_ID);
        List<String> taskTypes = taskTypeDTOs.stream().map(LovValueDTO::getValue).collect(Collectors.toList());

        List<Integer> badTaskIndex = new ArrayList<>();
        for(int i=0;i< tasks.size();i++) {
            if(!taskTypes.contains(tasks.get(i).getTaskType())){
                badTaskIndex.add(i+1);
            }
        }

        if(!badTaskIndex.isEmpty()){
            throw new CommonException(Constants.SAVE_TASK_ERROR_MULTILINGUAL,
                    badTaskIndex.size(), taskTypes.toString(), badTaskIndex.toString());
        }

        List<Task> insertList = tasks.stream().filter(line -> line.getId() == null).collect(Collectors.toList());
        List<Task> updateList = tasks.stream().filter(line -> line.getId() != null).collect(Collectors.toList());

        String ruleCode = "DEMO-47360";
        String levelCode = "GLOBAL";
        String levelValue = "GLOBAL";
        Map<String,String> variableParam = new HashMap<>();
        variableParam.put("customSegment","-"+ DetailsHelper.getUserDetails().getRealName()+"-");
        List<String> taskNumbers = codeRuleBuilder.generateCode(insertList.size(),
            organizationId, ruleCode, levelCode, levelValue,variableParam);

        for(int i=0;i<taskNumbers.size();i++){
            insertList.get(i).setTaskNumber(taskNumbers.get(i));
        }

        taskRepository.batchInsertSelective(insertList);
        taskRepository.batchUpdateByPrimaryKeySelective(updateList);
    }
}

