package com.hand.demo.domain.dto;

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
public class IamUserDTO implements Cacheable {
    private Long id; // 创建人ID

    @CacheValue(key = HZeroCacheKey.USER, primaryKey = "id", searchKey = "realName",
            structure = CacheValue.DataStructure.MAP_OBJECT)
    private String realName; // 创建人姓名

    @CacheValue(key = HZeroCacheKey.USER, primaryKey = "id", searchKey = "loginName",
            structure = CacheValue.DataStructure.MAP_OBJECT)
    private String loginName; // 创建人姓名

}
