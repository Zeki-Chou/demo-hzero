package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.OrderHeader47358;
import lombok.Getter;
import lombok.Setter;
import org.hzero.common.HZeroCacheKey;
import org.hzero.core.cache.CacheValue;
import org.hzero.core.cache.Cacheable;

import java.util.List;

@Getter
@Setter
public class OrderHeaderDTO extends OrderHeader47358 implements Cacheable {
    private IamDTO supplier;
    private List<IamDTO> buyers;
    private IamDTO creator;
}
