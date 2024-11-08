package com.hand.demo.app.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.hzero.core.redis.RedisQueueHelper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JobHandler("EXAM-47359-JOB-HANDLER")
public class ExamJob implements IJobHandler {

    private final InvoiceApplyHeaderRepository repository;
    private final ObjectMapper objectMapper;
    private final RedisQueueHelper redisQueueHelper;

    public ExamJob(InvoiceApplyHeaderRepository repository, ObjectMapper objectMapper, RedisQueueHelper redisQueueHelper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.redisQueueHelper = redisQueueHelper;
    }

    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {

        tool.info("START: Executing Task to cache failed header approval");

        List<InvoiceApplyHeader> invoiceApplyHeaderList = repository.selectAll();

        List<InvoiceApplyHeader> validInvoiceHeaderList = invoiceApplyHeaderList
                .stream()
                .filter(
                        invoiceApplyHeader -> invoiceApplyHeader.getDelFlag() != 1 &&
                        invoiceApplyHeader.getApplyStatus().equals("F") &&
                        invoiceApplyHeader.getInvoiceType().equals("E") &&
                        invoiceApplyHeader.getInvoiceColor().equals("R")
                ).collect(Collectors.toList());

        try {
            InvoiceInfoQueue invoiceInfoQueue = new InvoiceInfoQueue();
            invoiceInfoQueue.setEmployeeId("47359");
            String listHeaderString = objectMapper.writeValueAsString(validInvoiceHeaderList);
            invoiceInfoQueue.setContent(listHeaderString);
            String invoiceInfoString = objectMapper.writeValueAsString(invoiceInfoQueue);
            redisQueueHelper.push("invoiceInfo_47359", invoiceInfoString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return ReturnT.SUCCESS;
    }
}
