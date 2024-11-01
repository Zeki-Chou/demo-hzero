package com.hand.demo.infra.repository.impl;

import org.apache.commons.collections.CollectionUtils;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.OrderHeader47360;
import com.hand.demo.domain.repository.OrderHeader47360Repository;
import com.hand.demo.infra.mapper.OrderHeader47360Mapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * Inventory Count Header Table(OrderHeader47360)资源库
 *
 * @author
 * @since 2024-11-01 10:48:42
 */
@Component
public class OrderHeader47360RepositoryImpl extends BaseRepositoryImpl<OrderHeader47360> implements OrderHeader47360Repository {
    @Resource
    private OrderHeader47360Mapper orderHeader47360Mapper;

    @Override
    public List<OrderHeader47360> selectList(OrderHeader47360 orderHeader47360) {
        return orderHeader47360Mapper.selectList(orderHeader47360);
    }

    @Override
    public OrderHeader47360 selectByPrimary(Long id) {
        OrderHeader47360 orderHeader47360 = new OrderHeader47360();
        orderHeader47360.setId(id);
        List<OrderHeader47360> orderHeader47360s = orderHeader47360Mapper.selectList(orderHeader47360);
        if (orderHeader47360s.size() == 0) {
            return null;
        }
        return orderHeader47360s.get(0);
    }

}

