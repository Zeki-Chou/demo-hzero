package com.hand.demo.domain.repository;

import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.Task;

import java.util.List;

/**
 * 任务表(Task)资源库
 *
 * @author
 * @since 2024-11-01 08:10:06
 */
public interface TaskRepository extends BaseRepository<Task> {
    /**
     * 查询
     *
     * @param task 查询条件
     * @return 返回值
     */
    List<Task> selectList(Task task);

    /**
     * 根据主键查询（可关联表）
     *
     * @param id 主键
     * @return 返回值
     */
    Task selectByPrimary(Long id);
}
