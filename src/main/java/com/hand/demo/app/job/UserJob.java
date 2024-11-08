package com.hand.demo.app.job;

import com.hand.demo.app.service.MessageService;
import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static java.lang.Long.parseLong;

@JobHandler("USER_JOB_47357")
public class UserJob implements IJobHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UserJob.class);

    private final MessageService messageService;

    @Autowired
    public UserJob(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {
        LOG.info("Test Job 47357: Executing FlyBook message");

        Long userId = parseLong(map.get("ID"));

        messageService.flybookMessage(0L, userId);

        LOG.info("FlyBook message sent successfully.");
        return ReturnT.SUCCESS;
    }
}
