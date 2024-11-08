package com.hand.demo.app.service.job;

import com.alibaba.fastjson.JSON;
import com.hand.demo.app.service.Listener.QueueListener;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisQueueHelper;
import org.hzero.mybatis.domian.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@JobHandler("Invoice_Apply_Header_Job_47356")
public class ScheduleJob implements IJobHandler {
    @Autowired
    InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    RedisQueueHelper redisQueueHelper;

    @Autowired
    QueueListener queueListener;

    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {
        InvoiceInfoQueue invoiceInfoQueue = new InvoiceInfoQueue();
        Condition condition = new Condition(InvoiceApplyHeader.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andNotEqualTo(InvoiceApplyHeader.FIELD_DEL_FLAG, 1);
        criteria.andEqualTo(InvoiceApplyHeader.FIELD_APPLY_STATUS, "F");
        criteria.andEqualTo(InvoiceApplyHeader.FIELD_INVOICE_COLOR, "R");
        criteria.andEqualTo(InvoiceApplyHeader.FIELD_INVOICE_TYPE, "E");
        List<InvoiceApplyHeader> listHeader = invoiceApplyHeaderRepository.selectByCondition(condition);

        if (!listHeader.isEmpty()) {
            invoiceInfoQueue.setTenantId(BaseConstants.DEFAULT_TENANT_ID);
            invoiceInfoQueue.setEmployeeId("47356");
            invoiceInfoQueue.setContent(JSON.toJSONString(listHeader));
            redisQueueHelper.push("invoice-info-47356", JSON.toJSONString(invoiceInfoQueue));
            return ReturnT.SUCCESS;
        } else {
            return ReturnT.FAILURE;
        }
    }
}
