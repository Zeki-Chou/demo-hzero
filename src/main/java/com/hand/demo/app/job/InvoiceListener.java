package com.hand.demo.app.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.domain.repository.InvoiceInfoQueueRepository;
import io.choerodon.core.exception.CommonException;
import org.hzero.core.redis.handler.IQueueHandler;
import org.hzero.core.redis.handler.QueueHandler;


@QueueHandler("invoice-info-47357")
public class InvoiceListener implements IQueueHandler {

    private final InvoiceInfoQueueRepository invoiceInfoQueueRepository;
    private final ObjectMapper objectMapper;

    public InvoiceListener(InvoiceInfoQueueRepository invoiceInfoQueueRepository, ObjectMapper objectMapper) {
        this.invoiceInfoQueueRepository = invoiceInfoQueueRepository;
        this.objectMapper = objectMapper;
    }


    @Override
    public void process(String message) {
        try {
            InvoiceInfoQueue invoiceInfoQueue = objectMapper.readValue(message, InvoiceInfoQueue.class);
            // Wrap the invoiceInfoQueue in a list and pass it to saveData
            invoiceInfoQueueRepository.insert(invoiceInfoQueue);
        } catch (JsonProcessingException e) {
            throw new CommonException(e);
        }
    }

}
