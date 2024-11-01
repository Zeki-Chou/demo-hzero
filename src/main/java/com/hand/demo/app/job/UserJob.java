package com.hand.demo.app.job;

import com.hand.demo.app.service.MessageService;
import com.hand.demo.app.service.UserService;
import com.hand.demo.domain.entity.User;
import lombok.AllArgsConstructor;
import org.hzero.boot.message.entity.Message;
import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@JobHandler("DEMO_USER_JOB_47355")
@AllArgsConstructor
public class UserJob implements IJobHandler {
    private static Logger LOG = LoggerFactory.getLogger(UserJob.class);

    private UserService userService;
    private MessageService messageService;

    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {
        LOG.info("Tes user job 47355 " + map.get("id") + " " + map.get("empNumber"));
        Long id = Long.parseLong(map.get("id"));

//        Message msg = messageService.sendMessageToFlyBook(id);
//        LOG.info("Feishu msg" + msg);
        return ReturnT.SUCCESS;
    }
}
