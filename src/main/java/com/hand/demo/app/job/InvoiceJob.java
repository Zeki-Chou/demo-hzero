package com.hand.demo.app.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.hzero.core.redis.RedisQueueHelper;
import org.hzero.mybatis.domian.Condition;

import java.util.List;
import java.util.Map;

@JobHandler("INVOICE-APPLY-HEADER-47357")
public class InvoiceJob implements IJobHandler {
    private final RedisQueueHelper redisQueueHelper;
    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    private final ObjectMapper objectMapper;

    public InvoiceJob(RedisQueueHelper redisQueueHelper,
                      InvoiceApplyHeaderRepository invoiceApplyHeaderRepository,
                      ObjectMapper objectMapper) {
        this.redisQueueHelper = redisQueueHelper;
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
        this.objectMapper = objectMapper;
    }
    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {
        try {

            Condition condition = new Condition(InvoiceApplyHeader.class);
            Condition.Criteria criteria = condition.createCriteria();

            // Conditions for query
            criteria.andEqualTo("delFlag", 0);
            criteria.andEqualTo("applyStatus", "F");
            criteria.andEqualTo("invoiceColor", "R");
            criteria.andEqualTo("invoiceType", "E");

            // Query the database
            List<InvoiceApplyHeader> combinedResults = invoiceApplyHeaderRepository.selectByCondition(condition);

            // Convert to JSON string
            String json = objectMapper.writeValueAsString(combinedResults);

            // Push the JSON string to the Redis queue
            String redisQueueName = "invoice-info-47357";
            redisQueueHelper.push(redisQueueName, json);

            return ReturnT.SUCCESS;
        } catch (Exception e) {

            // Log
            e.printStackTrace();
            return ReturnT.FAILURE;
        }
    }

}
