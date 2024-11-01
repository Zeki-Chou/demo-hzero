package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.OrderHeader47360;
import lombok.Getter;
import lombok.Setter;
import org.hzero.core.cache.Cacheable;

import java.util.List;

@Getter
@Setter
public class OrderHeader47360DTO extends OrderHeader47360 implements Cacheable {
    private List<IamUserDTO> buyers;
    private IamUserDTO supplier;
    private IamUserDTO creator;
}
