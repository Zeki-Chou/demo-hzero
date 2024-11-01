package com.hand.demo.app.job;

import com.hand.demo.app.service.MessageService;
import org.hzero.boot.message.MessageClient;
import org.hzero.boot.message.entity.Message;
import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@JobHandler("USER-47360")
public class UserJob implements IJobHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserJob.class);
    private final MessageService messageService;

    @Autowired
    public UserJob( MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {
        Long employeeId = Long.valueOf(map.get("employeeId"));
        String email = map.get("email");
        Long organizationId =  Long.valueOf(map.get("organizationId"));

        Message message = messageService.flybook(employeeId,email,organizationId);
        logger.info("user job 47360: " + message.getContent());
        return ReturnT.SUCCESS;
    }
}
