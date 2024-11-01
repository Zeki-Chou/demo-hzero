package com.hand.demo.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(value = "hzero-platform")
public interface HPFMFeign {
    @GetMapping("/v1/online-users/list")
    ResponseEntity<List<Object>> onlineUserList();
}