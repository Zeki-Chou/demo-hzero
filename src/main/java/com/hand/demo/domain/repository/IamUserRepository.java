package com.hand.demo.domain.repository;

import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.IamUser;

import java.util.List;

/**
 * 用户(IamUser)资源库
 *
 * @author Allan
 * @since 2024-11-19 09:26:53
 */
public interface IamUserRepository extends BaseRepository<IamUser> {
    /**
     * 查询
     *
     * @param iamUser 查询条件
     * @return 返回值
     */
    List<IamUser> selectList(IamUser iamUser);

    /**
     * 根据主键查询（可关联表）
     *
     * @param id 主键
     * @return 返回值
     */
    IamUser selectByPrimary(Long id);
}
