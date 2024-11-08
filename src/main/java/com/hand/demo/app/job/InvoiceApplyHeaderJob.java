package com.hand.demo.app.job;


import com.alibaba.fastjson.JSON;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import lombok.AllArgsConstructor;
import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.hzero.core.redis.RedisQueueHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


@JobHandler("Invoice_Apply_Header_Job_47361")
@AllArgsConstructor
public class InvoiceApplyHeaderJob implements IJobHandler {
    private static final Logger LOG = LoggerFactory.getLogger(InvoiceApplyHeaderJob.class);
    private InvoiceApplyHeaderService invoiceApplyHeaderService;
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    private RedisQueueHelper redisQueueHelper;

    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {
        String invoiceHeaderJSON = getInvoiceJSON(0, "F", "R", "E");
        String keyRedisQueue = "Invoice-info-47361";

        InvoiceInfoQueue invoiceInfoQueue = new InvoiceInfoQueue();
        invoiceInfoQueue.setContent(invoiceHeaderJSON);
        invoiceInfoQueue.setEmployeeId("47361");
        invoiceInfoQueue.setTenantId(0L);

        String queueJSON = JSON.toJSONString(invoiceInfoQueue);

        redisQueueHelper.push(keyRedisQueue, queueJSON);

        return ReturnT.SUCCESS;
    }


    private String getInvoiceJSON(Integer delFlag,
                                  String invoiceStatusCode,
                                  String invoiceColorCode, String invoiceTypeCode){
        InvoiceApplyHeader headerExample = new InvoiceApplyHeader();
        headerExample.setDelFlag(delFlag);
        headerExample.setApplyStatus(invoiceStatusCode);
        headerExample.setInvoiceType(invoiceTypeCode);
        headerExample.setInvoiceColor(invoiceColorCode);
        List<InvoiceApplyHeader> headers = invoiceApplyHeaderRepository.select(headerExample);

        return JSON.toJSONString(headers);
    }
}
