package com.hand.demo.app.service;

import com.hand.demo.api.dto.OrderHeaderDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.OrderHeader47355;

import java.util.List;

/**
 * (OrderHeader47355)应用服务
 *
 * @author
 * @since 2024-11-01 12:27:36
 */
public interface OrderHeader47355Service {

    /**
     * 查询数据
     *
     * @param pageRequest       分页参数
     * @param orderHeader47355s 查询条件
     * @return 返回值
     */
    Page<OrderHeader47355> selectList(PageRequest pageRequest, OrderHeader47355 orderHeader47355s);

    /**
     * 保存数据
     *
     * @param orderHeader47355s 数据
     */
    void saveData(List<OrderHeader47355> orderHeader47355s);

    List<OrderHeaderDTO> getOrders(OrderHeader47355 orderHeader47355);

}

