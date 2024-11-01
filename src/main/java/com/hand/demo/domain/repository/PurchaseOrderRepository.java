package com.hand.demo.domain.repository;

import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.PurchaseOrder;

import java.util.List;

/**
 * (PurchaseOrder)资源库
 *
 * @author
 * @since 2024-11-01 16:59:20
 */
public interface PurchaseOrderRepository extends BaseRepository<PurchaseOrder> {
    /**
     * 查询
     *
     * @param purchaseOrder 查询条件
     * @return 返回值
     */
    List<PurchaseOrder> selectList(PurchaseOrder purchaseOrder);

    /**
     * 根据主键查询（可关联表）
     *
     * @param id 主键
     * @return 返回值
     */
    PurchaseOrder selectByPrimary(Long id);
}
