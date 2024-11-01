package com.hand.demo.infra.repository.impl;

import com.hand.demo.domain.entity.OrderHeader47356;
import com.hand.demo.domain.repository.OrderHeader47356Repository;
import com.hand.demo.infra.mapper.OrderHeader47356Mapper;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * (OrderHeader47356)资源库
 *
 * @author
 * @since 2024-11-01 13:17:14
 */
@Component
public class OrderHeader47356RepositoryImpl extends BaseRepositoryImpl<OrderHeader47356> implements OrderHeader47356Repository {
    @Resource
    private OrderHeader47356Mapper orderHeader47356Mapper;

    @Override
    public List<OrderHeader47356> selectList(OrderHeader47356 orderHeader47356) {
        return orderHeader47356Mapper.selectList(orderHeader47356);
    }

    @Override
    public OrderHeader47356 selectByPrimary(Long id) {
        OrderHeader47356 orderHeader47356 = new OrderHeader47356();
        orderHeader47356.setId(id);
        List<OrderHeader47356> orderHeader47356s = orderHeader47356Mapper.selectList(orderHeader47356);
        if (orderHeader47356s.size() == 0) {
            return null;
        }
        return orderHeader47356s.get(0);
    }

}

