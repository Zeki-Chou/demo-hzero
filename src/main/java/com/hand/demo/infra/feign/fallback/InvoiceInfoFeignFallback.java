package com.hand.demo.infra.feign.fallback;

import com.hand.demo.api.dto.InvoiceInfoFeignDTO;
import com.hand.demo.infra.feign.InvoiceInfoFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InvoiceInfoFeignFallback implements InvoiceInfoFeign {
    @Override
    public String receiveInvoiceInfo(InvoiceInfoFeignDTO invoiceInfoFeignDTO) {
        log.error("Error sending invoice");
        return null;
    }
}
