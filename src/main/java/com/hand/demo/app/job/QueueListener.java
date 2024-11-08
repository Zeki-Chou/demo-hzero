package com.hand.demo.app.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.domain.repository.InvoiceInfoQueueRepository;
import io.choerodon.core.exception.CommonException;
import org.hzero.core.redis.handler.IBatchQueueHandler;
import org.hzero.core.redis.handler.IQueueHandler;
import org.hzero.core.redis.handler.QueueHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@QueueHandler("invoice-info-47358")
public class QueueListener implements IQueueHandler {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    InvoiceInfoQueueRepository invoiceInfoQueueRepository;

    @Override
    public void process(String message) {
        try {
            InvoiceInfoQueue invoiceInfoQueue = objectMapper.readValue(message, InvoiceInfoQueue.class);
            invoiceInfoQueueRepository.insert(invoiceInfoQueue);
        } catch (JsonProcessingException e) {
            throw new CommonException(e);
        }

    }
}
