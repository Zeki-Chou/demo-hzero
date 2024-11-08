package com.hand.demo.app.service.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.domain.repository.InvoiceInfoQueueRepository;
import org.hzero.core.redis.handler.IQueueHandler;
import org.hzero.core.redis.handler.QueueHandler;

@QueueHandler("invoiceInfo_47359")
public class ExamJobListener implements IQueueHandler {

    private final ObjectMapper objectMapper;
    private final InvoiceInfoQueueRepository repository;

    public ExamJobListener(ObjectMapper objectMapper, InvoiceInfoQueueRepository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @Override
    public void process(String message) {
        try {
            InvoiceInfoQueue queue = objectMapper.readValue(message, InvoiceInfoQueue.class);
            repository.insertSelective(queue);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
