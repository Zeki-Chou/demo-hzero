package com.hand.demo.infra.feign.fallback;

import com.hand.demo.infra.feign.HPFMFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class HPFMFeignFallBack implements HPFMFeign {
    @Override
    public ResponseEntity<List<Object>> onlineUserList() {
        log.error("Error Feign /v1/online-users/list");
        return null;
    }
}
