package com.hand.demo.infra.mapper;

import com.hand.demo.api.dto.UserTasksDTO;
import com.hand.demo.domain.entity.User;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表Mapper
 *
 * @author joseph.julio@hand-global.com 2024-10-17 13:56:56
 */
public interface UserMapper extends BaseMapper<User> {
    List<UserTasksDTO> findUsersWithTasks(
            @Param("employeeNumber") String employeeNumber,
            @Param("taskNumber") String taskNumber
    );

    /**
     * 基础查询
     *
     * @param user 查询条件
     * @return 返回值
     */
    List<User> selectList(User user);
}
