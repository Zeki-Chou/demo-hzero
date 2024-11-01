package com.hand.demo.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.OrderHeader47360;

import java.util.List;

/**
 * Inventory Count Header Table(OrderHeader47360)应用服务
 *
 * @author
 * @since 2024-11-01 10:48:41
 */
public interface OrderHeader47360Mapper extends BaseMapper<OrderHeader47360> {
    /**
     * 基础查询
     *
     * @param orderHeader47360 查询条件
     * @return 返回值
     */
    List<OrderHeader47360> selectList(OrderHeader47360 orderHeader47360);
}

