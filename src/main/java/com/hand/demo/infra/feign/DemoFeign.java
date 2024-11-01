package com.hand.demo.infra.feign;

import com.hand.demo.domain.entity.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * FeignDemo
 */
@FeignClient(value = "demo", path = "/v1/{organizationId}")
public interface DemoFeign {

}
