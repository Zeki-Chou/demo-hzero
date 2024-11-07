package com.hand.demo.app.job;

import com.hand.demo.app.service.MessageService;
import org.hzero.boot.message.entity.Message;
import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@JobHandler("DEMO-47358-NEW")
public class UserJob implements IJobHandler {
    @Autowired
    MessageService messageService;

    Logger logger = LoggerFactory.getLogger(UserJob.class);
    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {
        logger.info("Test Job 47358 with user id : " + map.get("userId"));

        Message message = messageService.sendFeishu(Long.valueOf(map.get("userId")), 0L,
                map.get("email"));
        return ReturnT.SUCCESS;
    }


}
