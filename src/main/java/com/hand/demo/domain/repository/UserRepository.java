package com.hand.demo.domain.repository;

import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.User;

import java.util.List;

/**
 * 用户(User)资源库
 *
 * @author
 * @since 2024-11-18 08:28:23
 */
public interface UserRepository extends BaseRepository<User> {
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
