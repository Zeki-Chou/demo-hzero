package com.hand.demo.domain.repository;

import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.OrderHeader47355;

import java.util.List;

/**
 * (OrderHeader47355)资源库
 *
 * @author
 * @since 2024-11-01 12:27:36
 */
public interface OrderHeader47355Repository extends BaseRepository<OrderHeader47355> {
    /**
     * 查询
     *
     * @param orderHeader47355 查询条件
     * @return 返回值
     */
    List<OrderHeader47355> selectList(OrderHeader47355 orderHeader47355);

    /**
     * 根据主键查询（可关联表）
     *
     * @param id 主键
     * @return 返回值
     */
    OrderHeader47355 selectByPrimary(Long id);
}
