package com.hand.demo.infra.repository.impl;

import org.apache.commons.collections.CollectionUtils;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.OrderHeader47358;
import com.hand.demo.domain.repository.OrderHeader47358Repository;
import com.hand.demo.infra.mapper.OrderHeader47358Mapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * (OrderHeader47358)资源库
 *
 * @author
 * @since 2024-11-01 10:52:45
 */
@Component
public class OrderHeader47358RepositoryImpl extends BaseRepositoryImpl<OrderHeader47358> implements OrderHeader47358Repository {
    @Resource
    private OrderHeader47358Mapper orderHeader47358Mapper;

    @Override
    public List<OrderHeader47358> selectList(OrderHeader47358 orderHeader47358) {
        return orderHeader47358Mapper.selectList(orderHeader47358);
    }

    @Override
    public OrderHeader47358 selectByPrimary(Long id) {
        OrderHeader47358 orderHeader47358 = new OrderHeader47358();
        orderHeader47358.setId(id);
        List<OrderHeader47358> orderHeader47358s = orderHeader47358Mapper.selectList(orderHeader47358);
        if (orderHeader47358s.size() == 0) {
            return null;
        }
        return orderHeader47358s.get(0);
    }

}

