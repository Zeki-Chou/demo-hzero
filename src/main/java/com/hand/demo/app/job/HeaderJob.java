package com.hand.demo.app.job;

import com.alibaba.fastjson.JSON;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.hzero.core.redis.RedisQueueHelper;
import org.hzero.mybatis.domian.Condition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@JobHandler("INVOICE-APPLY-HEADER-47358")
public class HeaderJob implements IJobHandler {
    @Autowired
    InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    RedisQueueHelper redisQueueHelper;

    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {
        Condition condition = new Condition(InvoiceApplyHeader.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andNotEqualTo(InvoiceApplyHeader.FIELD_DEL_FLAG, 1);
        criteria.andNotEqualTo(InvoiceApplyHeader.FIELD_APPLY_STATUS, "F");
        criteria.andNotEqualTo(InvoiceApplyHeader.FIELD_INVOICE_COLOR, "R");
        criteria.andNotEqualTo(InvoiceApplyHeader.FIELD_INVOICE_TYPE, "E");
        List<InvoiceApplyHeader> listHeader = invoiceApplyHeaderRepository.selectByCondition(condition);

        if (!listHeader.isEmpty()) {
            redisQueueHelper.push("invoice-info-47358", JSON.toJSONString(listHeader));
            return ReturnT.SUCCESS;
        } else {
            return ReturnT.FAILURE;
        }
    }
}
