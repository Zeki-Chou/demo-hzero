package com.hand.demo.app.mq;

import com.alibaba.fastjson.JSON;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.domain.repository.InvoiceInfoQueueRepository;
import lombok.AllArgsConstructor;
import org.hzero.core.redis.handler.IQueueHandler;
import org.hzero.core.redis.handler.QueueHandler;

import java.util.List;

@QueueHandler("invoiceInfo_47355")
@AllArgsConstructor
public class InvoiceApplyHeaderListener  implements IQueueHandler {

    private InvoiceInfoQueueRepository invoiceInfoQueueRepository;

    @Override
    public void process(String message) {
        InvoiceInfoQueue invoiceInfoQueues = JSON.parseObject(message, InvoiceInfoQueue.class);

        invoiceInfoQueueRepository.insert(invoiceInfoQueues);
    }
}
