package com.hand.demo.infra.repository.impl;

import org.apache.commons.collections.CollectionUtils;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.Order47357;
import com.hand.demo.domain.repository.Order47357Repository;
import com.hand.demo.infra.mapper.Order47357Mapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * (Order47357)资源库
 *
 * @author
 * @since 2024-11-01 10:46:44
 */
@Component
public class Order47357RepositoryImpl extends BaseRepositoryImpl<Order47357> implements Order47357Repository {
    @Resource
    private Order47357Mapper order47357Mapper;

    @Override
    public List<Order47357> selectList(Order47357 order47357) {
        return order47357Mapper.selectList(order47357);
    }

    @Override
    public Order47357 selectByPrimary(Long id) {
        Order47357 order47357 = new Order47357();
        order47357.setId(id);
        List<Order47357> order47357s = order47357Mapper.selectList(order47357);
        if (order47357s.size() == 0) {
            return null;
        }
        return order47357s.get(0);
    }

}

