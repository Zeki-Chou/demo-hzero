package com.hand.demo.app.service;

import com.hand.demo.api.dto.UserCacheDTO;
import com.hand.demo.domain.entity.OrderHeader47356;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;

/**
 * (OrderHeader47356)应用服务
 *
 * @author
 * @since 2024-11-01 13:17:14
 */
public interface OrderHeader47356Service {

    /**
     * 查询数据
     *
     * @param pageRequest       分页参数
     * @param orderHeader47356s 查询条件
     * @return 返回值
     */
    Page<OrderHeader47356> selectList(PageRequest pageRequest, OrderHeader47356 orderHeader47356s);

    /**
     * 保存数据
     *
     * @param orderHeader47356s 数据
     */
    void saveData(List<OrderHeader47356> orderHeader47356s);
    public UserCacheDTO getOrderById(Long organizationId, Long id);
}

