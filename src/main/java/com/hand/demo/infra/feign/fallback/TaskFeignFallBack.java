package com.hand.demo.infra.feign.fallback;

import com.hand.demo.infra.feign.TaskFeign;
import org.hzero.core.util.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskFeignFallBack implements TaskFeign {

    private static final Logger log = LoggerFactory.getLogger(TaskFeignFallBack.class);


    @Override
    public ResponseEntity<List<Object>> onlineUserList() {
        log.error("Feign getting task detail error");
        return Results.error();
    }
}
