package com.hand.demo.app.job;

import com.alibaba.fastjson.JSON;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.domain.repository.InvoiceInfoQueueRepository;
import com.hand.demo.infra.constant.Constants;
import org.hzero.core.redis.handler.IBatchQueueHandler;
import org.hzero.core.redis.handler.QueueHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@QueueHandler(Constants.INVOICE_HEADER_QUEUE)
public class InvoiceApplyHandlerQueueListener implements IBatchQueueHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserJob.class);
    @Autowired
    private InvoiceInfoQueueRepository invoiceInfoQueueRepository;

    @Override
    public void process(List<String> messages) {
        List<InvoiceInfoQueue> invoiceInfoQueues= new ArrayList<>();
        for (String message:messages){
            InvoiceInfoQueue invoiceInfoQueue = JSON.parseObject(message,InvoiceInfoQueue.class);
            invoiceInfoQueues.add(invoiceInfoQueue);
        }
        invoiceInfoQueueRepository.batchInsertSelective(invoiceInfoQueues);
        logger.info("PROCESS QUEUE: "+invoiceInfoQueues);
    }
}