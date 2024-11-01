package com.hand.demo.domain.repository;

import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.Order47357;

import java.util.List;

/**
 * (Order47357)资源库
 *
 * @author
 * @since 2024-11-01 10:46:44
 */
public interface Order47357Repository extends BaseRepository<Order47357> {
    /**
     * 查询
     *
     * @param order47357 查询条件
     * @return 返回值
     */
    List<Order47357> selectList(Order47357 order47357);

    /**
     * 根据主键查询（可关联表）
     *
     * @param id 主键
     * @return 返回值
     */
    Order47357 selectByPrimary(Long id);
}
