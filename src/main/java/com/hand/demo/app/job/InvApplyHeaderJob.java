package com.hand.demo.app.job;

import com.alibaba.fastjson.JSON;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.units.qual.C;
import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.hzero.core.redis.RedisQueueHelper;
import org.hzero.mybatis.domian.Condition;

import java.util.List;
import java.util.Map;

@JobHandler("EXAM-47355-APPLY-HEADER")
@AllArgsConstructor
public class InvApplyHeaderJob implements IJobHandler {

    private RedisQueueHelper redisQueueHelper;

    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {
        Condition condition = new Condition(InvoiceApplyHeader.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria
                .andEqualTo("delFlag", 0)
                .andEqualTo("applyStatus", "F")
                .andEqualTo("invoiceColor", "R")
                .andEqualTo("invoiceType", "E");


        List<InvoiceApplyHeader> headers = invoiceApplyHeaderRepository.selectByCondition(condition);

        String headersJson = JSON.toJSONString(headers);

        InvoiceInfoQueue invoiceInfoQueue = new InvoiceInfoQueue();
        invoiceInfoQueue.setEmployeeId("47355");
        invoiceInfoQueue.setContent(headersJson);
        invoiceInfoQueue.setTenantId(0L);

        String queueJson = JSON.toJSONString(invoiceInfoQueue);

        String key = "invoiceInfo_47355";
        redisQueueHelper.push(key, queueJson);
        return ReturnT.SUCCESS;
    }
}
