package com.hand.demo.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.Task;

import java.util.List;

/**
 * 任务表(Task)应用服务
 *
 * @author
 * @since 2024-10-28 14:57:35
 */
public interface TaskService {

    /**
     * 查询数据
     *
     * @param pageRequest 分页参数
     * @param tasks       查询条件
     * @return 返回值
     */
    Page<Task> selectList(PageRequest pageRequest, Task tasks);

    /**
     * 保存数据
     *
     * @param tasks 数据
     */
    List<Task> saveData(List<Task> tasks);

    String createTaskWithCodeRule();

    List<Object> findTaskDetailFeign(Task task, Long organizationId, PageRequest pageRequest);
}

