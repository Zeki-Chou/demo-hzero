package com.hand.demo.infra.mapper;

import com.hand.demo.domain.entity.OrderHeader47356;
import io.choerodon.mybatis.common.BaseMapper;

import java.util.List;

/**
 * (OrderHeader47356)应用服务
 *
 * @author
 * @since 2024-11-01 13:17:14
 */
public interface OrderHeader47356Mapper extends BaseMapper<OrderHeader47356> {
    /**
     * 基础查询
     *
     * @param orderHeader47356 查询条件
     * @return 返回值
     */
    List<OrderHeader47356> selectList(OrderHeader47356 orderHeader47356);
}

