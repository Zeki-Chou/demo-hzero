package com.hand.demo.domain.repository;

import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.OrderHeader47361;

import java.util.List;

/**
 * Order Header Table(OrderHeader47361)资源库
 *
 * @author
 * @since 2024-11-01 10:52:08
 */
public interface OrderHeader47361Repository extends BaseRepository<OrderHeader47361> {
    /**
     * 查询
     *
     * @param orderHeader47361 查询条件
     * @return 返回值
     */
    List<OrderHeader47361> selectList(OrderHeader47361 orderHeader47361);

    /**
     * 根据主键查询（可关联表）
     *
     * @param orderId 主键
     * @return 返回值
     */
    OrderHeader47361 selectByPrimary(Long orderId);
}
