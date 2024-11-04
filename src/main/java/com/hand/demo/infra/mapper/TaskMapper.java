package com.hand.demo.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.Task;

import java.util.List;

/**
 * 任务表(Task)应用服务
 *
 * @author
 * @since 2024-10-28 14:57:35
 */
public interface TaskMapper extends BaseMapper<Task> {
    /**
     * 基础查询
     *
     * @param task 查询条件
     * @return 返回值
     */
    List<Task> selectList(Task task);
}

