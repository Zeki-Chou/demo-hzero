package com.hand.demo.infra.mapper;

import com.hand.demo.api.controller.dto.UserTaskInfoDTO;
import com.hand.demo.domain.entity.User;
import io.choerodon.mybatis.common.BaseMapper;

import java.util.List;

/**
 * 用户表Mapper
 *
 * @author allan.sugianto@hand-global.com 2024-10-17 14:34:10
 */
public interface UserMapper extends BaseMapper<User> {
    /**
     * 基础查询
     * @param user 查询条件
     * @return 返回值
     */
    List<User> selectList(User user);
    List<User> selectUserTask();
    List<UserTaskInfoDTO> selectUserWithTask(UserTaskInfoDTO dto);
}
