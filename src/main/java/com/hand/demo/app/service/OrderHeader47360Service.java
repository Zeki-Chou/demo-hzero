package com.hand.demo.app.service;

import com.hand.demo.api.dto.OrderHeader47360DTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.OrderHeader47360;
import io.choerodon.core.domain.Page;

import java.util.List;

/**
 * Inventory Count Header Table(OrderHeader47360)应用服务
 *
 * @author
 * @since 2024-11-01 10:48:42
 */
public interface OrderHeader47360Service {

    /**
     * 查询数据
     *
     * @param pageRequest       分页参数
     * @param orderHeader47360s 查询条件
     * @return 返回值
     */
    Page<OrderHeader47360DTO> selectList(PageRequest pageRequest, OrderHeader47360 orderHeader47360s);

    OrderHeader47360DTO detail(Long organizationId, Long id);

    /**
     * 保存数据
     *
     * @param orderHeader47360s 数据
     * @return
     */
    List<OrderHeader47360DTO> saveData(List<OrderHeader47360> orderHeader47360s);

}

