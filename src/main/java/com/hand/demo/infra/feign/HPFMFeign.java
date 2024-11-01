package com.hand.demo.infra.feign;

import com.hand.demo.domain.entity.Task;
import io.choerodon.core.domain.Page;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "hzero-platform")
public interface HPFMFeign {
    @GetMapping("/v1/online-users/list")
    ResponseEntity<List<Object>> onlineUserList();
}