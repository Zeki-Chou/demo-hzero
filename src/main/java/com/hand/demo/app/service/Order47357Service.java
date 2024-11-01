package com.hand.demo.app.service;

import com.hand.demo.api.dto.ImUserDto;
import com.hand.demo.api.dto.OrderResponseDto;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.Order47357;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * (Order47357)应用服务
 *
 * @author
 * @since 2024-11-01 10:46:44
 */
public interface Order47357Service {

    /**
     * 查询数据
     *
     * @param pageRequest 分页参数
     * @param order47357s 查询条件
     * @return 返回值
     */
    List<OrderResponseDto> selectList(Order47357 order47357s);

    /**
     * 保存数据
     *
     * @param order47357s 数据
     */
    List<OrderResponseDto> saveData(List<Order47357> order47357s);



}

