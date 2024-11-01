package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.Order47357;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hzero.core.cache.Cacheable;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto  extends Order47357 implements Cacheable{
    private List<ImUserDto> buyers;
    private ImUserDto supplier;
    private ImUserDto creator;
}
