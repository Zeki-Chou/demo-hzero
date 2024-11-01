package com.hand.demo.infra.feign;

import com.hand.demo.domain.entity.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "demo-47358")
public interface OtherTaskFeign {
    @PostMapping("/v1/{organizationId}/tasks")
    List<Task> save(@PathVariable Long organizationId, @RequestBody List<Task> tasks);
}
