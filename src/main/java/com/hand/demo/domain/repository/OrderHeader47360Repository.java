package com.hand.demo.domain.repository;

import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.OrderHeader47360;

import java.util.List;

/**
 * Inventory Count Header Table(OrderHeader47360)资源库
 *
 * @author
 * @since 2024-11-01 10:48:42
 */
public interface OrderHeader47360Repository extends BaseRepository<OrderHeader47360> {
    /**
     * 查询
     *
     * @param orderHeader47360 查询条件
     * @return 返回值
     */
    List<OrderHeader47360> selectList(OrderHeader47360 orderHeader47360);

    /**
     * 根据主键查询（可关联表）
     *
     * @param id 主键
     * @return 返回值
     */
    OrderHeader47360 selectByPrimary(Long id);
}
