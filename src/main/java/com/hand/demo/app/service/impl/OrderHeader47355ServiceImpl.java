package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.BuyerDTO;
import com.hand.demo.api.dto.OrderHeaderDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.OrderHeader47355Service;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.OrderHeader47355;
import com.hand.demo.domain.repository.OrderHeader47355Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * (OrderHeader47355)应用服务
 *
 * @author
 * @since 2024-11-01 12:27:36
 */
@Service
public class OrderHeader47355ServiceImpl implements OrderHeader47355Service {
    @Autowired
    private OrderHeader47355Repository orderHeader47355Repository;

    @Override
    public Page<OrderHeader47355> selectList(PageRequest pageRequest, OrderHeader47355 orderHeader47355) {
        return PageHelper.doPageAndSort(pageRequest, () -> orderHeader47355Repository.selectList(orderHeader47355));
    }

    @Override
    public void saveData(List<OrderHeader47355> orderHeader47355s) {
        List<OrderHeader47355> insertList = orderHeader47355s.stream().filter(line -> line.getId() == null).collect(Collectors.toList());
        List<OrderHeader47355> updateList = orderHeader47355s.stream().filter(line -> line.getId() != null).collect(Collectors.toList());
        orderHeader47355Repository.batchInsertSelective(insertList);
        orderHeader47355Repository.batchUpdateByPrimaryKeySelective(updateList);
    }

    @Override
    public List<OrderHeaderDTO> getOrders(OrderHeader47355 orderHeader47355) {
        List<OrderHeader47355> orderHeader47355s = orderHeader47355Repository.selectList(orderHeader47355);
        return orderHeader47355s.stream()
                .map(order -> {
                    OrderHeaderDTO orderHeaderDTO = new OrderHeaderDTO();
                    BeanUtils.copyProperties(order, orderHeaderDTO);
                    orderHeaderDTO.setBuyers(getBuyers(order.getBuyerId().toString()));
                    return orderHeaderDTO;
                })
                .collect(Collectors.toList());
    }

    private List<BuyerDTO> getBuyers(String buyerIds) {
        List<String> ids = Arrays.asList(buyerIds.split(","));
        return ids.stream().map(id -> new BuyerDTO().setBuyerId(Long.parseLong(id))).collect(Collectors.toList());
    }
}

