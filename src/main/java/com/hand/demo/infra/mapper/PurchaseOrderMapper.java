package com.hand.demo.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.PurchaseOrder;

import java.util.List;

/**
 * (PurchaseOrder)应用服务
 *
 * @author
 * @since 2024-11-01 16:59:20
 */
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {
    /**
     * 基础查询
     *
     * @param purchaseOrder 查询条件
     * @return 返回值
     */
    List<PurchaseOrder> selectList(PurchaseOrder purchaseOrder);
}

