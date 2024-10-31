package com.hand.demo.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.Task;

import java.util.List;

/**
 * 任务表(Task)应用服务
 *
 * @author Zeki
 * @since 2024-10-31 14:04:52
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
    void saveData(List<Task> tasks);

}

