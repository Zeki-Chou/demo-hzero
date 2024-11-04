package com.hand.demo.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.User;

import java.util.List;

/**
 * User Table(User)应用服务
 *
 * @author
 * @since 2024-10-31 10:27:59
 */
public interface UserService {

    /**
     * 查询数据
     *
     * @param pageRequest 分页参数
     * @param users       查询条件
     * @return 返回值
     */
    Page<User> selectList(PageRequest pageRequest, User users);

    /**
     * 保存数据
     *
     * @param users 数据
     */
    void saveData(List<User> users);

    /**
     * 用户表查询参数
     *
     * @param tenantId 租户ID
     * @param user 用户表
     * @param pageRequest 分页
     * @return 用户表列表
     */
    Page<User> list(Long tenantId, User user, PageRequest pageRequest);

    /**
     * 用户表详情
     *
     * @param tenantId 租户ID
     * @param id 主键
     * @return 用户表列表
     */
    User detail(Long tenantId, Long id);

    /**
     * 创建用户表
     *
     * @param tenantId 租户ID
     * @param user 用户表
     * @return 用户表
     */
    User create(Long tenantId, User user);

    /**
     * 更新用户表
     *
     * @param tenantId 租户ID
     * @param user 用户表
     * @return 用户表
     */
    User update(Long tenantId, User user);

    /**
     * 删除用户表
     *
     * @param user 用户表
     */
    void remove(User user);

}

