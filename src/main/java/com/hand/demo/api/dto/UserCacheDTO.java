package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.OrderHeader47356;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hzero.common.HZeroCacheKey;
import org.hzero.core.cache.CacheValue;
import org.hzero.core.cache.Cacheable;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCacheDTO extends OrderHeader47356 implements Cacheable {
    private Long user_id;
    private List<UserCacheListDTO> list_buyer;

    @CacheValue(
            key = HZeroCacheKey.USER,
            primaryKey = "user_id",
            searchKey = "realName",
            structure = CacheValue.DataStructure.MAP_OBJECT
    )
    private String realName;

    @CacheValue(
            key = HZeroCacheKey.USER,
            primaryKey = "supplierId",
            searchKey = "realName",
            structure = CacheValue.DataStructure.MAP_OBJECT
    )
    private String supplier_name;

//    @CacheValue(
//            key = HZeroCacheKey.USER,
//            primaryKey = "buyerIds",
//            searchKey = "realName",
//            structure = CacheValue.DataStructure.MAP_OBJECT
//    )
//    private String buyer_name;

    @CacheValue(
            key = HZeroCacheKey.USER,
            primaryKey = "createdBy",
            searchKey = "realName",
            structure = CacheValue.DataStructure.MAP_OBJECT
    )
    private String creator_name;
}