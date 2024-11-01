package com.hand.demo.infra.feign.fallback;

import com.hand.demo.domain.entity.Task;
import com.hand.demo.infra.feign.DemoFeign;
import org.hzero.core.util.Results;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DemoFeignFallBack
 */
@Component
public class DemoFeignFallBack implements DemoFeign {

}
