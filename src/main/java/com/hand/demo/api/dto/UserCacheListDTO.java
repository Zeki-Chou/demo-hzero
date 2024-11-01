package com.hand.demo.api.dto;

import lombok.Getter;
import lombok.Setter;
import org.hzero.common.HZeroCacheKey;
import org.hzero.core.cache.CacheValue;
import org.hzero.core.cache.Cacheable;

@Getter
@Setter
public class UserCacheListDTO implements Cacheable {
    private String id;
    @CacheValue(
            key = HZeroCacheKey.USER,
            primaryKey = "id",
            searchKey = "realName",
            structure = CacheValue.DataStructure.MAP_OBJECT
    )
    private String buyer_name;
}
