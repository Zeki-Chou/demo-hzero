package com.hand.demo.app.job;

import com.hand.demo.app.service.MessageService;
import lombok.AllArgsConstructor;
import org.hzero.boot.message.entity.Message;
import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@JobHandler("USER_JOB_47361")
@AllArgsConstructor
public class UserInfoJob implements IJobHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UserInfoJob.class);

    @Autowired
    private MessageService messageService;

    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {
        long organizationId = 0L;
        long userId = Long.parseLong(map.get("userId"));

        Message message = messageService.sendFeshu(organizationId,userId);
        LOG.info("Feshu send :" + message);

        return ReturnT.SUCCESS;
    }

}

