package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.ImUserDto;
import com.hand.demo.api.dto.OrderResponseDto;
import com.hand.demo.infra.constant.TaskConstants;
import io.choerodon.core.domain.Page;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.core.cache.ProcessCacheValue;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.Order47357Service;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.Order47357;
import com.hand.demo.domain.repository.Order47357Repository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class Order47357ServiceImpl implements Order47357Service {

    @Autowired
    private Order47357Repository order47357Repository;

    @Autowired
    private CodeRuleBuilder codeRuleBuilder;

    @Override
    @ProcessCacheValue
    public List<OrderResponseDto> selectList(Order47357 order47357) {
        return order47357Repository.selectList(order47357).stream()
                .map(order -> createOrderResponseDto(order, true))
                .collect(Collectors.toList());
    }

    @Override
    @ProcessCacheValue
    public List<OrderResponseDto> saveData(List<Order47357> order47357s) {
        List<Order47357> insertList = order47357s.stream()
                .filter(line -> line.getId() == null)
                .collect(Collectors.toList());
        List<Order47357> updateList = order47357s.stream()
                .filter(line -> line.getId() != null)
                .collect(Collectors.toList());

        insertList.forEach(order -> {
            Map<String, String> variableMap = new HashMap<>();
            variableMap.put("customSegment", ("-" + DetailsHelper.getUserDetails().getUsername()) + "-");
            String uniquePurchaseNumber = codeRuleBuilder.generateCode("PO-47357", variableMap);
            order.setPurchaseNumber(uniquePurchaseNumber);
        });

        order47357Repository.batchInsertSelective(insertList);
        order47357Repository.batchUpdateByPrimaryKeySelective(updateList);

        List<OrderResponseDto> responseDtos = new ArrayList<>();

        responseDtos.addAll(insertList.stream()
                .map(order -> createOrderResponseDto(order, false))
                .collect(Collectors.toList()));

        responseDtos.addAll(updateList.stream()
                .map(order -> createOrderResponseDto(order, false))
                .collect(Collectors.toList()));

        return responseDtos;
    }

    private OrderResponseDto createOrderResponseDto(Order47357 order, boolean includeCreator) {
        OrderResponseDto responseDto = new OrderResponseDto();
        BeanUtils.copyProperties(order, responseDto);

        List<ImUserDto> buyers = getBuyers(order.getBuyerIds());
        responseDto.setBuyers(buyers);

        responseDto.setSupplier(fetchImUserDtoById(order.getSupplierId()));

        if (includeCreator) {
            responseDto.setCreator(fetchImUserDtoById(order.getCreatedBy()));
        }

        return responseDto;
    }

    private List<ImUserDto> getBuyers(String buyerIds) {
        return Arrays.stream(buyerIds.split(","))
                .map(Long::parseLong)
                .map(this::fetchImUserDtoById)
                .collect(Collectors.toList());
    }

    private ImUserDto fetchImUserDtoById(Long id) {
        ImUserDto user = new ImUserDto();
        user.setId(id);
        return user;
    }
}
