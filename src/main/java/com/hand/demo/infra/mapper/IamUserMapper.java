package com.hand.demo.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.IamUser;

import java.util.List;

/**
 * 用户(IamUser)应用服务
 *
 * @author Allan
 * @since 2024-11-19 09:26:53
 */
public interface IamUserMapper extends BaseMapper<IamUser> {
    /**
     * 基础查询
     *
     * @param iamUser 查询条件
     * @return 返回值
     */
    List<IamUser> selectList(IamUser iamUser);
}

