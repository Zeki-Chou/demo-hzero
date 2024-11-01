package com.hand.demo.domain.repository;

import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.OrderHeader47358;

import java.util.List;

/**
 * (OrderHeader47358)资源库
 *
 * @author
 * @since 2024-11-01 10:52:45
 */
public interface OrderHeader47358Repository extends BaseRepository<OrderHeader47358> {
    /**
     * 查询
     *
     * @param orderHeader47358 查询条件
     * @return 返回值
     */
    List<OrderHeader47358> selectList(OrderHeader47358 orderHeader47358);

    /**
     * 根据主键查询（可关联表）
     *
     * @param id 主键
     * @return 返回值
     */
    OrderHeader47358 selectByPrimary(Long id);
}
