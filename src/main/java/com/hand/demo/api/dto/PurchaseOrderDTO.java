package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.PurchaseOrder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hzero.common.HZeroCacheKey;
import org.hzero.core.cache.CacheValue;
import org.hzero.core.cache.Cacheable;

import java.util.List;

// DTO to find createdBy, supplier and buyer real name
@Getter
@Setter
public class PurchaseOrderDTO extends PurchaseOrder implements Cacheable {

    List<BuyerDTO> buyerNames;

    @CacheValue(key = HZeroCacheKey.USER, primaryKey = "supplierId", searchKey = "realName", structure = CacheValue.DataStructure.MAP_OBJECT)
    private String supplierName;

    @CacheValue(key = HZeroCacheKey.USER, primaryKey = "createdBy", searchKey = "realName", structure = CacheValue.DataStructure.MAP_OBJECT)
    private String creatorName;
}
