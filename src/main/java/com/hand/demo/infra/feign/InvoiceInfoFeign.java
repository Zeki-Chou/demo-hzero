package com.hand.demo.infra.feign;

import com.hand.demo.api.dto.InvoiceInfoFeignDTO;
import com.hand.demo.domain.entity.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "hzero-project-20740",path = "v1/example")
public interface InvoiceInfoFeign {
    @PostMapping("/receive/invoice")
    String receiveInvoiceInfo(@RequestBody InvoiceInfoFeignDTO invoiceInfoFeignDTO);

}
