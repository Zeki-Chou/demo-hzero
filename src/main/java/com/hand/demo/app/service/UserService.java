package com.hand.demo.app.service;

import com.hand.demo.api.dto.UserTasksDTO;
import com.hand.demo.api.dto.UserTasksRequest;
import com.hand.demo.domain.entity.User;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;

/**
 * 用户表应用服务
 *
 * @author joseph.julio@hand-global.com 2024-10-17 13:56:56
 */
public interface UserService {

    /**
     * 用户表查询参数
     *
     * @param tenantId    租户ID
     * @param user        用户表
     * @param pageRequest 分页
     * @return 用户表列表
     */
    Page<User> list(Long tenantId, User user, PageRequest pageRequest);

    List<UserTasksDTO> getUserWithTasks(Long tenantId, User user, PageRequest pageRequest);

    List<UserTasksDTO> getUsersWithTasks(UserTasksDTO userTasksDTO);


    /**
     * 用户表详情
     *
     * @param tenantId 租户ID
     * @param id       主键
     * @return 用户表列表
     */
    User detail(Long tenantId, Long id);

    /**
     * 创建用户表
     *
     * @param tenantId 租户ID
     * @param user     用户表
     * @return 用户表
     */
    User create(Long tenantId, User user);

    /**
     * 更新用户表
     *
     * @param tenantId 租户ID
     * @param user     用户表
     * @return 用户表
     */
    User update(Long tenantId, User user);

    /**
     * 删除用户表
     *
     * @param user 用户表
     */
    void remove(User user);

    List<User> saveData(List<User> users);
}