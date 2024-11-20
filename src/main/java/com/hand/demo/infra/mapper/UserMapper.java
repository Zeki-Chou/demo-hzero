package com.hand.demo.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.User;

import java.util.List;

/**
 * 用户(User)应用服务
 *
 * @author
 * @since 2024-11-18 08:28:23
 */
public interface UserMapper extends BaseMapper<User> {
    /**
     * 基础查询
     *
     * @param user 查询条件
     * @return 返回值
     */
    List<User> selectList(User user);
}

