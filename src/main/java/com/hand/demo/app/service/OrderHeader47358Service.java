package com.hand.demo.app.service;

import com.hand.demo.api.dto.OrderHeaderDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.OrderHeader47358;

import java.util.List;

/**
 * (OrderHeader47358)应用服务
 *
 * @author
 * @since 2024-11-01 10:52:45
 */
public interface OrderHeader47358Service {

    /**
     * 查询数据
     *
     * @param pageRequest       分页参数
     * @param orderHeader47358s 查询条件
     * @return 返回值
     */
    Page<OrderHeaderDTO> selectList(PageRequest pageRequest, OrderHeader47358 orderHeader47358s);

//    List<OrderHeaderDTO> selectList(PageRequest pageRequest, OrderHeader47358 orderHeader47358s);
    /**
     * 保存数据
     *
     * @param orderHeader47358s 数据
     */
//    void saveData(List<OrderHeader47358> orderHeader47358s);

    List<OrderHeaderDTO> saveData(List<OrderHeader47358> orderHeader47358s);

    List<OrderHeaderDTO> getList();

    OrderHeaderDTO getDetail(Long id);
}

