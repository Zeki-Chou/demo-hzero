package com.hand.demo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hzero.common.HZeroCacheKey;
import org.hzero.core.cache.CacheValue;
import org.hzero.core.cache.Cacheable;

@Getter
@Setter
@Accessors(chain = true)
public class BuyerDTO implements Cacheable {
    private Long buyerId;
    @CacheValue(key = HZeroCacheKey.USER, primaryKey = "buyerId", searchKey = "realName",
            structure = CacheValue.DataStructure.MAP_OBJECT)
    private String buyerName;
}
