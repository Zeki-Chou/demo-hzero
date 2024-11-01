package com.hand.demo.domain.repository;

import com.hand.demo.domain.entity.OrderHeader47356;
import org.hzero.mybatis.base.BaseRepository;

import java.util.List;

/**
 * (OrderHeader47356)资源库
 *
 * @author
 * @since 2024-11-01 13:17:14
 */
public interface OrderHeader47356Repository extends BaseRepository<OrderHeader47356> {
    /**
     * 查询
     *
     * @param orderHeader47356 查询条件
     * @return 返回值
     */
    List<OrderHeader47356> selectList(OrderHeader47356 orderHeader47356);

    /**
     * 根据主键查询（可关联表）
     *
     * @param id 主键
     * @return 返回值
     */
    OrderHeader47356 selectByPrimary(Long id);
}
