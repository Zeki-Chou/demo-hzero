package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.IamUserDTO;
import com.hand.demo.api.dto.OrderHeader47360DTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.core.cache.ProcessCacheValue;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.OrderHeader47360Service;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.OrderHeader47360;
import com.hand.demo.domain.repository.OrderHeader47360Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Inventory Count Header Table(OrderHeader47360)应用服务
 *
 * @author
 * @since 2024-11-01 10:48:42
 */
@Service
public class OrderHeader47360ServiceImpl implements OrderHeader47360Service {
    @Autowired
    private OrderHeader47360Repository orderHeader47360Repository;

    @ProcessCacheValue
    @Override
    public Page<OrderHeader47360DTO> selectList(PageRequest pageRequest, OrderHeader47360 orderHeader47360) {
        Page<OrderHeader47360> orderHeader47360Page = PageHelper.doPageAndSort(pageRequest, () -> orderHeader47360Repository.selectList(orderHeader47360));

        Page<OrderHeader47360DTO> orderHeader47360DTOPage = new Page<>();
        BeanUtils.copyProperties(orderHeader47360Page,orderHeader47360DTOPage);

        List<OrderHeader47360DTO> orderHeader47360DTOS = new ArrayList<>();
        for (OrderHeader47360 orderHeader47360InPage:orderHeader47360Page.getContent()){
            orderHeader47360DTOS.add(generateOrderHeader74360DTO(orderHeader47360InPage));
        }
        orderHeader47360DTOPage.setContent(orderHeader47360DTOS);
        return  orderHeader47360DTOPage;
    }

    @ProcessCacheValue
    @Override
    public OrderHeader47360DTO detail(Long organizationId, Long id) {
        OrderHeader47360 orderHeader47360 = orderHeader47360Repository.selectByPrimary(id);
        return generateOrderHeader74360DTO(orderHeader47360);
    }

    @ProcessCacheValue
    @Override
    public List<OrderHeader47360DTO> saveData(List<OrderHeader47360> orderHeader47360s) {
        List<OrderHeader47360> insertList = orderHeader47360s.stream().filter(line -> line.getId() == null).collect(Collectors.toList());
        List<OrderHeader47360> updateList = orderHeader47360s.stream().filter(line -> line.getId() != null).collect(Collectors.toList());
        orderHeader47360Repository.batchInsertSelective(insertList);
        orderHeader47360Repository.batchUpdateByPrimaryKeySelective(updateList);

        List<OrderHeader47360DTO> orderHeader47360DTOS = new ArrayList<>();
        for(OrderHeader47360 orderHeader47360:orderHeader47360s){
            orderHeader47360DTOS.add(generateOrderHeader74360DTO(orderHeader47360));
        }
        return orderHeader47360DTOS;
    }

    private OrderHeader47360DTO generateOrderHeader74360DTO(OrderHeader47360 orderHeader47360){
        List<IamUserDTO> buyerDTOS = new ArrayList<>();
        for (String buyerId: orderHeader47360.getBuyerIds().split(",")){
            IamUserDTO buyerDTO = new IamUserDTO();
            buyerDTO.setId(Long.parseLong(buyerId));
            buyerDTOS.add(buyerDTO);
        }
        IamUserDTO supplierDTO = new IamUserDTO();
        supplierDTO.setId(orderHeader47360.getSupplierId());
        IamUserDTO creatorDTO = new IamUserDTO();
        creatorDTO.setId(orderHeader47360.getCreatedBy());

        OrderHeader47360DTO orderHeader47360DTO = new OrderHeader47360DTO();
        BeanUtils.copyProperties(orderHeader47360,orderHeader47360DTO);
        orderHeader47360DTO.setBuyers(buyerDTOS);
        orderHeader47360DTO.setSupplier(supplierDTO);
        orderHeader47360DTO.setCreator(creatorDTO);
        return orderHeader47360DTO;
    }
}

