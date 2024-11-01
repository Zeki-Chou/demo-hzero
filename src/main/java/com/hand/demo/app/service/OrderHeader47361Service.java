package com.hand.demo.app.service;

import com.hand.demo.domain.dto.OrderHeaderDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.OrderHeader47361;

import java.util.List;

/**
 * Order Header Table(OrderHeader47361)应用服务
 *
 * @author
 * @since 2024-11-01 10:52:08
 */
public interface OrderHeader47361Service {

    /**
     * 查询数据
     *
     * @param pageRequest       分页参数
     * @param orderHeader47361s 查询条件
     * @return 返回值
     */
    Page<OrderHeader47361> selectList(PageRequest pageRequest, OrderHeader47361 orderHeader47361s);

    List<OrderHeaderDTO> selectListDetail(OrderHeader47361 orderHeader47361);

    /**
     * 保存数据
     *
     * @param orderHeader47361s 数据
     */
    void saveData(List<OrderHeader47361> orderHeader47361s);

}

