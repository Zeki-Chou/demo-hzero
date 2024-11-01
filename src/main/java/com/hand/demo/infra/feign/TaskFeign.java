package com.hand.demo.infra.feign;

import com.hand.demo.domain.entity.Task;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

//@ComponentScan
//@FeignClient(name = "demo-47357-new", url = "172.22.4.112:8080")
//public interface TaskFeign {
//
//    @GetMapping("/v1/{organizationId}/tasks")
//    Page<Task> list(@RequestParam Task task, @PathVariable Long organizationId,
//                    @ApiIgnore @SortDefault(value = Task.FIELD_ID,
//                            direction = Sort.Direction.DESC) PageRequest pageRequest);
//
//    @PostMapping("/v1/{organizationId}/tasks")
//    List<Task> save(@PathVariable Long organizationId, @RequestBody List<Task> tasks);
//
//}
@ComponentScan
@FeignClient(value = "hzero-platform")
public interface TaskFeign {
    @GetMapping("/v1/online-users/list")
    ResponseEntity<List<Object>> onlineUserList();
}
