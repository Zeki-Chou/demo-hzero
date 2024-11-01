package com.hand.demo.app.service;

import com.hand.demo.api.dto.PurchaseOrderDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.PurchaseOrder;

import java.util.List;

/**
 * (PurchaseOrder)应用服务
 *
 * @author
 * @since 2024-11-01 16:59:21
 */
public interface PurchaseOrderService {

    /**
     * 查询数据
     *
     * @param pageRequest    分页参数
     * @param purchaseOrders 查询条件
     * @return 返回值
     */
    Page<PurchaseOrder> selectList(PageRequest pageRequest, PurchaseOrder purchaseOrders);

    /**
     * 保存数据
     *
     * @param purchaseOrders 数据
     */
    void saveData(List<PurchaseOrder> purchaseOrders);

    PurchaseOrderDTO getPurchaseOrderDetail(Long id);

    List<PurchaseOrderDTO> getListPurchaseOrder(PurchaseOrder purchaseOrder);

}

