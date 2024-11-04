package com.hand.demo.infra.feign;

import com.hand.demo.domain.entity.Task;
import com.hand.demo.infra.feign.fallback.TaskFeignFallBack;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * reminder: switch to other service name later
 */
@FeignClient(value = "hzero-platform", fallback = TaskFeignFallBack.class)
public interface TaskFeign {
    @GetMapping("/v1/online-users/list")
    ResponseEntity<List<Object>> onlineUserList();
}
