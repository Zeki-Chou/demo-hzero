package com.hand.demo.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.Order47357;

import java.util.List;

/**
 * (Order47357)应用服务
 *
 * @author
 * @since 2024-11-01 10:46:44
 */
public interface Order47357Mapper extends BaseMapper<Order47357> {
    /**
     * 基础查询
     *
     * @param order47357 查询条件
     * @return 返回值
     */
    List<Order47357> selectList(Order47357 order47357);
}

