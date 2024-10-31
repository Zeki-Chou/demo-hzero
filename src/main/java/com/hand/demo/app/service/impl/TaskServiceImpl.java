package com.hand.demo.app.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.TaskService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.repository.TaskRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务表(Task)应用服务
 *
 * @author Zeki
 * @since 2024-10-31 14:04:52
 */
@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Override
    public Page<Task> selectList(PageRequest pageRequest, Task task) {
        return PageHelper.doPageAndSort(pageRequest, () -> taskRepository.selectList(task));
    }

    @Override
    public void saveData(List<Task> tasks) {
        List<Task> insertList = tasks.stream().filter(line -> line.getId() == null).collect(Collectors.toList());
        List<Task> updateList = tasks.stream().filter(line -> line.getId() != null).collect(Collectors.toList());
        taskRepository.batchInsertSelective(insertList);
        taskRepository.batchUpdateByPrimaryKeySelective(updateList);
    }
}

