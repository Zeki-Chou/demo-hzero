package com.hand.demo.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.OrderHeader47355;

import java.util.List;

/**
 * (OrderHeader47355)应用服务
 *
 * @author
 * @since 2024-11-01 12:27:36
 */
public interface OrderHeader47355Mapper extends BaseMapper<OrderHeader47355> {
    /**
     * 基础查询
     *
     * @param orderHeader47355 查询条件
     * @return 返回值
     */
    List<OrderHeader47355> selectList(OrderHeader47355 orderHeader47355);
}

