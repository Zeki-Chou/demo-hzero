package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.OrderHeader47355;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hzero.common.HZeroCacheKey;
import org.hzero.core.cache.CacheValue;
import org.hzero.core.cache.Cacheable;
import org.hzero.core.cache.ProcessCacheValue;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class OrderHeaderDTO extends OrderHeader47355 implements Cacheable {
    private Long orderId;

    private List<BuyerDTO> buyers;

    @CacheValue(key = HZeroCacheKey.USER, primaryKey = "supplierId", searchKey = "realName",
            structure = CacheValue.DataStructure.MAP_OBJECT)
    private String supplierName;

//    private Long createdBy;
    @CacheValue(key = HZeroCacheKey.USER, primaryKey = "createdBy", searchKey = "realName",
            structure = CacheValue.DataStructure.MAP_OBJECT)
    private String creatorName;

//    @Override
//    public void setCreatedBy(Long id) {
//        this.createdBy = id;
//    }
}
