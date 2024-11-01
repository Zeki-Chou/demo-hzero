package com.hand.demo.infra.feign.fallback;

import com.hand.demo.domain.entity.Task;
import com.hand.demo.infra.feign.OtherTaskFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OtherTaskFeignFallBack
 */
@Slf4j
@Component
public class OtherTaskFeignFallBack implements OtherTaskFeign {
    @Override
    public List<Task> save(Long organizationId, List<Task> tasks) {
        log.error("/v1/{organizationId}/tasks.organizationId: {}, tasks: {}",organizationId,tasks);
        return null;
    }
}
