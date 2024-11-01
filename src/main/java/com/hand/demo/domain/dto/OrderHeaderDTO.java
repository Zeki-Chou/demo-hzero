package com.hand.demo.domain.dto;

import com.hand.demo.domain.entity.OrderHeader47361;
import lombok.Getter;
import lombok.Setter;
import org.hzero.common.HZeroCacheKey;
import org.hzero.core.cache.CacheValue;
import org.hzero.core.cache.Cacheable;

import java.util.List;

@Getter
@Setter
public class OrderHeaderDTO extends OrderHeader47361 implements Cacheable {
    private List<IamUserDTO> listBuyer;

    //    private IamUserDTO supplier;

    //    private IamUserDTO supplier;

    //Kalau buat ini, jadi untuk supplier hanya memunculkan suppliername, yang id supplier nya dicari di database iam_user
    @CacheValue(key = HZeroCacheKey.USER, primaryKey = "supplierId", searchKey = "realName",
            structure = CacheValue.DataStructure.MAP_OBJECT)
    private String supplierName;



    @CacheValue(key = HZeroCacheKey.USER, primaryKey = "createdBy", searchKey = "realName",
            structure = CacheValue.DataStructure.MAP_OBJECT)
    private String creatorName;

}
