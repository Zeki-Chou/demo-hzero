package com.hand.demo.infra.feign;


import com.hand.demo.domain.entity.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * TaskFeign
 */
@ComponentScan
@FeignClient(value = "hzero-platform")
public interface TaskFeign {

    @GetMapping("/v1/online-users/list")
    ResponseEntity<List<Object>> onlineUserList();
}
