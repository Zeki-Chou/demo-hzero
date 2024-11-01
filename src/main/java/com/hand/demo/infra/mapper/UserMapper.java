package com.hand.demo.infra.mapper;

import com.hand.demo.domain.dto.UserTaskDTO;
import com.hand.demo.domain.entity.User;
import feign.Param;
import io.choerodon.mybatis.common.BaseMapper;

import java.util.List;

/**
 * 用户表Mapper
 *
 * @author azhar.naufal@hand-global.com 2024-10-17 13:48:26
 */
public interface UserMapper extends BaseMapper<User> {
    /**
     * 基础查询
     *
     * @param user 查询条件
     * @return 返回值
     */
    List<User> selectList(User user);

    List<User> selectAllUser();

    List<User> selectUserByEmpIds(List<Long> empIds);

    List<UserTaskDTO> selectUsersWithTasks();

    List<UserTaskDTO> selectUserTaskByEmpOrTaskNumber(@Param("employeeNumber") String employeeNumber, @Param("taskNumber") String taskNumber);

}
