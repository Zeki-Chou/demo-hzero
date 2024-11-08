package com.hand.demo.app.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.app.service.InvoiceInfoQueueService;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.domain.repository.InvoiceInfoQueueRepository;
import lombok.AllArgsConstructor;
import org.hzero.core.redis.RedisQueueHelper;
import org.hzero.core.redis.handler.IBatchQueueHandler;
import org.hzero.core.redis.handler.IQueueHandler;
import org.hzero.core.redis.handler.QueueHandler;

import java.util.List;

@QueueHandler("Invoice-info-47361")
@AllArgsConstructor
public class InvoiceListener implements IQueueHandler {
    private InvoiceInfoQueueService invoiceInfoQueueService;
    private InvoiceInfoQueueRepository invoiceInfoQueueRepository;
    private ObjectMapper objectMapper;


    @Override
    public void process(String message) {
        try {
            InvoiceInfoQueue invoiceInfoQueue = objectMapper.readValue(message, InvoiceInfoQueue.class);
            invoiceInfoQueueRepository.insert(invoiceInfoQueue);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
