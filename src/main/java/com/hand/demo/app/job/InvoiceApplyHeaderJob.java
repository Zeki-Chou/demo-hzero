package com.hand.demo.app.job;

import com.alibaba.fastjson.JSON;
import com.hand.demo.api.dto.InvoiceInfoFeignDTO;
import com.hand.demo.app.service.MessageService;
import com.hand.demo.domain.entity.InvCountHeader;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.Constants;
import com.hand.demo.infra.feign.InvoiceInfoFeign;
import org.hzero.boot.message.entity.Message;
import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.hzero.core.redis.RedisHelper;
import org.hzero.core.redis.RedisQueueHelper;
import org.hzero.mybatis.domian.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;


@JobHandler("INVOICE-APPLY-HEADER-47360")

public class InvoiceApplyHeaderJob implements IJobHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserJob.class);
    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    @Autowired
    private RedisQueueHelper redisQueueHelper;
    @Autowired
    private InvoiceInfoFeign invoiceInfoFeign;

    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {
        String contentJson = generateInvoiceInfoQueueJson();
        if(!contentJson.isEmpty()) {
            pushHeaders(contentJson);
//            sendToPlatform(contentJson);
        }
        return ReturnT.SUCCESS;
    }

    private String generateInvoiceInfoQueueJson(){
        Condition condition = new Condition(InvoiceApplyHeader.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo(InvoiceApplyHeader.FIELD_DEL_FLAG,0);
        criteria.andEqualTo(InvoiceApplyHeader.FIELD_APPLY_STATUS,"F");
        criteria.andEqualTo(InvoiceApplyHeader.FIELD_INVOICE_COLOR,"R");
        criteria.andEqualTo(InvoiceApplyHeader.FIELD_INVOICE_TYPE,"E");
        List<InvoiceApplyHeader> invoiceApplyHeaders= invoiceApplyHeaderRepository.selectByCondition(condition);

        InvoiceInfoQueue invoiceInfoQueue = new InvoiceInfoQueue();
        invoiceInfoQueue.setEmployeeId("47360");
        invoiceInfoQueue.setTenantId(0L);
        invoiceInfoQueue.setContent(JSON.toJSONString(invoiceApplyHeaders));

        return JSON.toJSONString(invoiceInfoQueue);
    }

    private void pushHeaders(String contentJson){
        redisQueueHelper.push(Constants.INVOICE_HEADER_QUEUE,contentJson);
    }

    private void sendToPlatform(String contentJson){
        InvoiceInfoFeignDTO invoiceInfoFeignDTO = new InvoiceInfoFeignDTO();
        invoiceInfoFeignDTO.setContent(contentJson);
        invoiceInfoFeignDTO.setEmployeeId("47360");

        String result = invoiceInfoFeign.receiveInvoiceInfo(invoiceInfoFeignDTO);
        logger.info("Job INVOICE-APPLY-HEADER-47360: "+result);
    }
}
