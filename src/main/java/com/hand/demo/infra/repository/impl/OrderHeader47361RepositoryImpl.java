package com.hand.demo.infra.repository.impl;

import org.apache.commons.collections.CollectionUtils;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.OrderHeader47361;
import com.hand.demo.domain.repository.OrderHeader47361Repository;
import com.hand.demo.infra.mapper.OrderHeader47361Mapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * Order Header Table(OrderHeader47361)资源库
 *
 * @author
 * @since 2024-11-01 10:52:08
 */
@Component
public class OrderHeader47361RepositoryImpl extends BaseRepositoryImpl<OrderHeader47361> implements OrderHeader47361Repository {
    @Resource
    private OrderHeader47361Mapper orderHeader47361Mapper;

    @Override
    public List<OrderHeader47361> selectList(OrderHeader47361 orderHeader47361) {
        return orderHeader47361Mapper.selectList(orderHeader47361);
    }

    @Override
    public OrderHeader47361 selectByPrimary(Long orderId) {
        OrderHeader47361 orderHeader47361 = new OrderHeader47361();
        orderHeader47361.setOrderId(orderId);
        List<OrderHeader47361> orderHeader47361s = orderHeader47361Mapper.selectList(orderHeader47361);
        if (orderHeader47361s.size() == 0) {
            return null;
        }
        return orderHeader47361s.get(0);
    }

}

