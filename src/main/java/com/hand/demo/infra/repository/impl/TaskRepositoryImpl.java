package com.hand.demo.infra.repository.impl;

import org.apache.commons.collections.CollectionUtils;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.Task;
import com.hand.demo.domain.repository.TaskRepository;
import com.hand.demo.infra.mapper.TaskMapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * 任务表(Task)资源库
 *
 * @author
 * @since 2024-10-31 16:47:18
 */
@Component
public class TaskRepositoryImpl extends BaseRepositoryImpl<Task> implements TaskRepository {
    @Resource
    private TaskMapper taskMapper;

    @Override
    public List<Task> selectList(Task task) {
        return taskMapper.selectList(task);
    }

    @Override
    public Task selectByPrimary(Long id) {
        Task task = new Task();
        task.setId(id);
        List<Task> tasks = taskMapper.selectList(task);
        if (tasks.size() == 0) {
            return null;
        }
        return tasks.get(0);
    }

}

