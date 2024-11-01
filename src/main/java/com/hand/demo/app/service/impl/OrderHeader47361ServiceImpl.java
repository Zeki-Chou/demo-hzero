package com.hand.demo.app.service.impl;

import com.hand.demo.domain.dto.IamUserDTO;
import com.hand.demo.domain.dto.OrderHeaderDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.OrderHeader47361Service;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.OrderHeader47361;
import com.hand.demo.domain.repository.OrderHeader47361Repository;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order Header Table(OrderHeader47361)应用服务
 *
 * @author
 * @since 2024-11-01 10:52:08
 */
@Service
public class OrderHeader47361ServiceImpl implements OrderHeader47361Service {
    @Autowired
    private OrderHeader47361Repository orderHeader47361Repository;

    @Override
    public Page<OrderHeader47361> selectList(PageRequest pageRequest, OrderHeader47361 orderHeader47361) {
        return PageHelper.doPageAndSort(pageRequest, () -> orderHeader47361Repository.selectList(orderHeader47361));
    }

    @Override
    public List<OrderHeaderDTO> selectListDetail(OrderHeader47361 orderHeader47361) {
        List<OrderHeader47361> orderHeaders = orderHeader47361Repository.selectList(orderHeader47361);

        List<OrderHeaderDTO> orderHeaderDTOS = new LinkedList<>();

        for (OrderHeader47361 order : orderHeaders) {
            OrderHeaderDTO orderHeaderDTO = new OrderHeaderDTO();
//            orderHeaderDTO.setOrderId(order.getOrderId());
//            orderHeaderDTO.setOrderNumber(order.getOrderNumber());
//            orderHeaderDTO.setOrderStatus(order.getOrderStatus());
//            orderHeaderDTO.setOrderDate(order.getOrderDate());
//            orderHeaderDTO.setRemark(order.getRemark());
//            orderHeaderDTO.setSupplierId(order.getSupplierId());
//            orderHeaderDTO.setTenantId(order.getTenantId());
//            orderHeaderDTO.setWorkflowId(order.getWorkflowId());
//            orderHeaderDTO.setApprovedTime(order.getApprovedTime());
            BeanUtils.copyProperties(order,orderHeaderDTO); //Ini untuk memudahkan, saat set/copy dari foreach dengan data yg sama


            List<IamUserDTO> buyers = new LinkedList<>();
            String buyerIds = order.getBuyerIds();
            for (String id : buyerIds.split(",")) {
                Long userId = Long.valueOf(id.trim());
                IamUserDTO buyerDetails = new IamUserDTO();
                buyerDetails.setId(userId);
                buyers.add(buyerDetails);
            }

            orderHeaderDTO.setListBuyer(buyers);


//            IamUserDTO supplierDetails = new IamUserDTO();
//            supplierDetails.setId(order.getSupplierId());
//            orderHeaderDTO.setSupplier(supplierDetails);


//            IamUserDTO creatorDetails = new IamUserDTO();
//            creatorDetails.setId(order.getCreatedBy());
//            orderHeaderDTO.setCreator(creatorDetails);


            orderHeaderDTOS.add(orderHeaderDTO);
        }

        return orderHeaderDTOS;
    }

    @Override
    public void saveData(List<OrderHeader47361> orderHeader47361s) {
        List<OrderHeader47361> insertList = orderHeader47361s.stream().filter(line -> line.getOrderId() == null).collect(Collectors.toList());
        List<OrderHeader47361> updateList = orderHeader47361s.stream().filter(line -> line.getOrderId() != null).collect(Collectors.toList());
        orderHeader47361Repository.batchInsertSelective(insertList);
        orderHeader47361Repository.batchUpdateByPrimaryKeySelective(updateList);
    }


}

