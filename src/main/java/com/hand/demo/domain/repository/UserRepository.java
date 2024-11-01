package com.hand.demo.domain.repository;

import com.hand.demo.api.dto.UserTasksDTO;
import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.User;

import java.util.List;

/**
 * 用户表资源库
 *
 * @author joseph.julio@hand-global.com 2024-10-17 13:56:56
 */
public interface UserRepository extends BaseRepository<User> {
    List<UserTasksDTO> findUsersWithTasks(
            String employeeNumber,
            String taskNumber
    );

    /**
     * 查询
     *
     * @param user 查询条件
     * @return 返回值
     */
    List<User> selectList(User user);

    /**
     * 根据主键查询（可关联表）
     *
     * @param id 主键
     * @return 返回值
     */
    User selectByPrimary(Long id);
}
