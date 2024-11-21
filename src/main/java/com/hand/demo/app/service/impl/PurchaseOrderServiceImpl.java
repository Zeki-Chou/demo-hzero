package com.hand.demo.app.service.impl;

import com.hand.demo.api.controller.dto.BuyerDTO;
import com.hand.demo.api.controller.dto.PurchaseOrderDTO;
import com.hand.demo.infra.constant.PurchaseStatus;
import com.hand.demo.infra.util.Utils;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.apache.avro.generic.GenericData;
import org.hzero.core.cache.ProcessCacheValue;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.PurchaseOrderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.PurchaseOrder;
import com.hand.demo.domain.repository.PurchaseOrderRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * (PurchaseOrder)应用服务
 *
 * @author Allan
 * @since 2024-11-01 10:50:58
 */
@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Override
    public Page<PurchaseOrder> selectList(PageRequest pageRequest, PurchaseOrder purchaseOrder) {
        return PageHelper.doPageAndSort(pageRequest, () -> purchaseOrderRepository.selectList(purchaseOrder));
    }

    @Override
    public void saveData(List<PurchaseOrder> purchaseOrders) {

        for (PurchaseOrder order: purchaseOrders) {
            // purchase status validation check
            if (!validPurchaseStatus(order.getCountStatus())) {
                throw new CommonException(order.getCountStatus() + " is invalid purchase status");
            }

            // id null check
            if (order.getSupplierId() == null || order.getBuyerIds() == null) {
                throw new CommonException("empty supplier and buyer Id");
            }
        }

        List<PurchaseOrder> insertList = purchaseOrders.stream().filter(line -> line.getId() == null).collect(Collectors.toList());
        List<PurchaseOrder> updateList = purchaseOrders.stream().filter(line -> line.getId() != null).collect(Collectors.toList());
        purchaseOrderRepository.batchInsertSelective(insertList);
        purchaseOrderRepository.batchUpdateByPrimaryKeySelective(updateList);
    }

    @Override
    @ProcessCacheValue
    public PurchaseOrderDTO getPurchaseOrderDetail(Long id) {
        return mapToDTO(purchaseOrderRepository.selectByPrimary(id));
    }

    @Override
    @ProcessCacheValue
    public List<PurchaseOrderDTO> getListPurchaseOrder(PurchaseOrder purchaseOrder) {
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.select(purchaseOrder);
        return purchaseOrders.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private PurchaseOrderDTO mapToDTO(PurchaseOrder purchaseOrder) {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();

        String[] buyerIdList = purchaseOrder.getBuyerIds().split(",");
        List<BuyerDTO> buyerList = new ArrayList<>();

        for (String buyerId: buyerIdList) {
            BuyerDTO buyer = new BuyerDTO();
            buyer.setId(buyerId);
            buyerList.add(buyer);
        }

        BeanUtils.copyProperties(purchaseOrder, dto);
        dto.setBuyerNames(buyerList);
        return dto;
    }

    private boolean validPurchaseStatus(String status) {
        for (PurchaseStatus s: PurchaseStatus.values()) {
            if (s.name().equals(status)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
}

