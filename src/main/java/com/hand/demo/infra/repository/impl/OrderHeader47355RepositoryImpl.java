package com.hand.demo.infra.repository.impl;

import org.apache.commons.collections.CollectionUtils;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.OrderHeader47355;
import com.hand.demo.domain.repository.OrderHeader47355Repository;
import com.hand.demo.infra.mapper.OrderHeader47355Mapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * (OrderHeader47355)资源库
 *
 * @author
 * @since 2024-11-01 12:27:36
 */
@Component
public class OrderHeader47355RepositoryImpl extends BaseRepositoryImpl<OrderHeader47355> implements OrderHeader47355Repository {
    @Resource
    private OrderHeader47355Mapper orderHeader47355Mapper;

    @Override
    public List<OrderHeader47355> selectList(OrderHeader47355 orderHeader47355) {
        return orderHeader47355Mapper.selectList(orderHeader47355);
    }

    @Override
    public OrderHeader47355 selectByPrimary(Long id) {
        OrderHeader47355 orderHeader47355 = new OrderHeader47355();
        orderHeader47355.setId(id);
        List<OrderHeader47355> orderHeader47355s = orderHeader47355Mapper.selectList(orderHeader47355);
        if (orderHeader47355s.size() == 0) {
            return null;
        }
        return orderHeader47355s.get(0);
    }

}

