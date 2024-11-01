package com.hand.demo.domain.repository;

import com.hand.demo.api.dto.UserDTO;
import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.User;

import java.util.List;

/**
 * 用户表资源库
 *
 * @author fatih.khoiri@hand-global.com 2024-10-17 13:57:07
 */
public interface UserRepository extends BaseRepository<User> {
    List<UserDTO> findUserTask(UserDTO userDTO);
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
