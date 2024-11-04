package com.hand.demo.infra.repository.impl;

import org.apache.commons.collections.CollectionUtils;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.PurchaseOrder;
import com.hand.demo.domain.repository.PurchaseOrderRepository;
import com.hand.demo.infra.mapper.PurchaseOrderMapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * (PurchaseOrder)资源库
 *
 * @author
 * @since 2024-11-01 10:50:58
 */
@Component
public class PurchaseOrderRepositoryImpl extends BaseRepositoryImpl<PurchaseOrder> implements PurchaseOrderRepository {
    @Resource
    private PurchaseOrderMapper purchaseOrderMapper;

    @Override
    public List<PurchaseOrder> selectList(PurchaseOrder purchaseOrder) {
        return purchaseOrderMapper.selectList(purchaseOrder);
    }

    @Override
    public PurchaseOrder selectByPrimary(Long id) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setId(id);
        List<PurchaseOrder> purchaseOrders = purchaseOrderMapper.selectList(purchaseOrder);
        if (purchaseOrders.size() == 0) {
            return null;
        }
        return purchaseOrders.get(0);
    }

}

