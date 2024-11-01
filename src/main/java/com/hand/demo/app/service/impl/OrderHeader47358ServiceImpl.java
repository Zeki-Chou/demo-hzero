package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.IamDTO;
import com.hand.demo.api.dto.OrderHeaderDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.core.cache.ProcessCacheValue;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.OrderHeader47358Service;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.OrderHeader47358;
import com.hand.demo.domain.repository.OrderHeader47358Repository;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * (OrderHeader47358)应用服务
 *
 * @author
 * @since 2024-11-01 10:52:45
 */
@Service
public class OrderHeader47358ServiceImpl implements OrderHeader47358Service {
    @Autowired
    private OrderHeader47358Repository orderHeader47358Repository;

    @Override
    @ProcessCacheValue
    public Page<OrderHeaderDTO> selectList(PageRequest pageRequest, OrderHeader47358 orderHeader47358) {
        Page<OrderHeader47358> pageResult = PageHelper.doPageAndSort(pageRequest, () ->
                orderHeader47358Repository.selectList(orderHeader47358));

        List<OrderHeaderDTO> orderHeaderDTOS = new ArrayList<>();
        for (OrderHeader47358 data : pageResult) {
            orderHeaderDTOS.add(convertToDTO(data));
        }

        Page<OrderHeaderDTO> dtoPage = new Page<>();
        dtoPage.setContent(orderHeaderDTOS);
        dtoPage.setTotalPages(pageResult.getTotalPages());
        dtoPage.setTotalElements(pageResult.getTotalElements());
        dtoPage.setNumber(pageResult.getNumber());
        dtoPage.setSize(pageResult.getSize());

        return dtoPage;
    }

    @Override
    @ProcessCacheValue
    public List<OrderHeaderDTO> saveData(List<OrderHeader47358> orderHeader47358s) {
        List<OrderHeader47358> insertList = orderHeader47358s.stream().
                filter(line -> line.getId() == null).
                collect(Collectors.toList());
        List<OrderHeader47358> updateList = orderHeader47358s.stream().
                filter(line -> line.getId() != null).
                collect(Collectors.toList());

        orderHeader47358Repository.batchInsertSelective(insertList);
        orderHeader47358Repository.batchUpdateByPrimaryKeySelective(updateList);

        List<OrderHeaderDTO> dtoList = new ArrayList<>();
        for (OrderHeader47358 order : orderHeader47358s) {
            OrderHeaderDTO dto = convertToDTO(order);
            dtoList.add(dto);
        }

        return dtoList;
    }

    @Override
    @ProcessCacheValue
    public List<OrderHeaderDTO> getList() {
        List<OrderHeader47358> orderList = orderHeader47358Repository.selectAll();
        List<OrderHeaderDTO> dtoList = new ArrayList<>();
        for (OrderHeader47358 order : orderList) {
            OrderHeaderDTO dto = convertToDTO(order);
            dtoList.add(dto);
        }

        return dtoList;
    }

    @Override
    @ProcessCacheValue
    public OrderHeaderDTO getDetail(Long id) {
        OrderHeader47358 orderHeader = orderHeader47358Repository.selectByPrimaryKey(id);

        if (orderHeader == null) {
            throw new CommonException("OrderHeader not found for id: " + id);
        }

        return convertToDTO(orderHeader);
    }

    private OrderHeaderDTO convertToDTO(OrderHeader47358 orderHeader) {
        OrderHeaderDTO dto = new OrderHeaderDTO();
        dto.setId(orderHeader.getId());
        dto.setOrderNumber(orderHeader.getOrderNumber());
        dto.setStatus(orderHeader.getStatus());
        dto.setObjectVersionNumber(orderHeader.getObjectVersionNumber());

        IamDTO supplier = new IamDTO();
        supplier.setId(orderHeader.getSupplierId());
        dto.setSupplier(supplier);

        IamDTO creator = new IamDTO();
        creator.setId(orderHeader.getCreatedBy());
        dto.setCreator(creator);

        String buyerIds = (String) orderHeader.getBuyerIds();
        List<IamDTO> buyerList = new ArrayList<>();
        if (buyerIds != null && !buyerIds.trim().isEmpty()) {
            String[] ids = buyerIds.split(",");
            for (String id : ids) {
                Long buyerId = Long.valueOf(id.trim());
                IamDTO buyer = new IamDTO();
                buyer.setId(buyerId);
                buyerList.add(buyer);
            }
        } else {
            buyerList = Collections.emptyList();
        }
        dto.setBuyers(buyerList);
        return dto;
    }



//    private OrderHeaderDTO convertToDTO(OrderHeader47358 orderHeader) {
//        OrderHeaderDTO dto = new OrderHeaderDTO();
//        dto.setId(orderHeader.getId());
//        dto.setOrderNumber(orderHeader.getOrderNumber());
//        dto.setSupplierId(orderHeader.getSupplierId());
//        dto.setBuyerIds(orderHeader.getBuyerIds());
//        dto.setStatus(orderHeader.getStatus());
//        dto.setCreatedBy(orderHeader.getCreatedBy());
//        dto.setObjectVersionNumber(orderHeader.getObjectVersionNumber());
//        return dto;
//    }
}

