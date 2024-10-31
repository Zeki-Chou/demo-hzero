package com.hand.demo.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.User;

import java.util.List;

/**
 * User Table(User)应用服务
 *
 * @author
 * @since 2024-10-31 16:43:06
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

}

