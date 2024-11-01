package com.hand.demo.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.OrderHeader47358;

import java.util.List;

/**
 * (OrderHeader47358)应用服务
 *
 * @author
 * @since 2024-11-01 10:52:45
 */
public interface OrderHeader47358Mapper extends BaseMapper<OrderHeader47358> {
    /**
     * 基础查询
     *
     * @param orderHeader47358 查询条件
     * @return 返回值
     */
    List<OrderHeader47358> selectList(OrderHeader47358 orderHeader47358);
}

