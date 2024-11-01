package com.hand.demo.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.OrderHeader47361;

import java.util.List;

/**
 * Order Header Table(OrderHeader47361)应用服务
 *
 * @author
 * @since 2024-11-01 10:52:08
 */
public interface OrderHeader47361Mapper extends BaseMapper<OrderHeader47361> {
    /**
     * 基础查询
     *
     * @param orderHeader47361 查询条件
     * @return 返回值
     */
    List<OrderHeader47361> selectList(OrderHeader47361 orderHeader47361);
}

