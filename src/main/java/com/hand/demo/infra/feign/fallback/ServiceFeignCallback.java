package com.hand.demo.infra.feign.fallback;

import com.hand.demo.domain.entity.Task;
import com.hand.demo.infra.feign.ServiceFeign;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
public class ServiceFeignCallback implements ServiceFeign {
    @Override
    public ResponseEntity<List<Object>> onlineUserList() {
        log.error("Theres an error while try access tgis");
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

//    @Override
//    public Page<Task> getListTask(Long organizationId) {
//        log.error("there's an error");
//        return new Page<>();
//    }
}
