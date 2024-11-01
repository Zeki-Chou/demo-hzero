package com.hand.demo.api.dto;

import lombok.Getter;
import lombok.Setter;
import org.hzero.common.HZeroCacheKey;
import org.hzero.core.cache.CacheValue;
import org.hzero.core.cache.Cacheable;

import java.util.Date;

@Getter
@Setter
public class IamUserDTO implements Cacheable {
    private Long id;

    @CacheValue(key = HZeroCacheKey.USER, primaryKey = "id", searchKey = "realName",
            structure = CacheValue.DataStructure.MAP_OBJECT)
    private String realName;

    @CacheValue(key = HZeroCacheKey.USER, primaryKey = "id", searchKey = "loginName",
            structure = CacheValue.DataStructure.MAP_OBJECT)
    private String loginName;

    @CacheValue(key = HZeroCacheKey.USER, primaryKey = "id", searchKey = "userType",
            structure = CacheValue.DataStructure.MAP_OBJECT)
    private String userType;

    @CacheValue(key = HZeroCacheKey.USER, primaryKey = "id", searchKey = "language",
            structure = CacheValue.DataStructure.MAP_OBJECT)
    private String language;
}

