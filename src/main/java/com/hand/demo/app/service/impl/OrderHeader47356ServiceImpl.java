package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.UserCacheDTO;
import com.hand.demo.api.dto.UserCacheListDTO;
import com.hand.demo.app.service.OrderHeader47356Service;
import com.hand.demo.domain.entity.OrderHeader47356;
import com.hand.demo.domain.repository.OrderHeader47356Repository;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.core.cache.ProcessCacheValue;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * (OrderHeader47356)应用服务
 *
 * @author
 * @since 2024-11-01 13:17:15
 */
@Service
public class OrderHeader47356ServiceImpl implements OrderHeader47356Service {
    @Autowired
    private OrderHeader47356Repository orderHeader47356Repository;

    @Override
    public Page<OrderHeader47356> selectList(PageRequest pageRequest, OrderHeader47356 orderHeader47356) {
        return PageHelper.doPageAndSort(pageRequest, () -> orderHeader47356Repository.selectList(orderHeader47356));
    }

    @Override
    public void saveData(List<OrderHeader47356> orderHeader47356s) {
        List<OrderHeader47356> insertList = orderHeader47356s.stream().filter(line -> line.getId() == null).collect(Collectors.toList());
        List<OrderHeader47356> updateList = orderHeader47356s.stream().filter(line -> line.getId() != null).collect(Collectors.toList());
        orderHeader47356Repository.batchInsertSelective(insertList);
        orderHeader47356Repository.batchUpdateByPrimaryKeySelective(updateList);
    }

    @ProcessCacheValue
    public UserCacheDTO getOrderById(Long organizationId, Long id) {
        OrderHeader47356 getOrder = orderHeader47356Repository.selectByPrimary(id);

        String[] buyer_list = getOrder.getBuyerIds().split(",");
        List<UserCacheListDTO> cache = new ArrayList<>();

        for(int i = 0; i < buyer_list.length; i++) {
            UserCacheListDTO user_cache = new UserCacheListDTO();
            user_cache.setId(buyer_list[i]);
            cache.add(user_cache);
        }

        UserCacheDTO response = new UserCacheDTO();
        response.setList_buyer(cache);
        BeanUtils.copyProperties(getOrder,response);

        return response;
    }
}

