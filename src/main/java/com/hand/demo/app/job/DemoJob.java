package com.hand.demo.app.job;

import com.hand.demo.app.service.UserService;
import com.hand.demo.domain.entity.User;
import com.hand.demo.infra.constant.MessageConstant;
import org.hzero.boot.message.MessageClient;
import org.hzero.boot.message.entity.FlyBookMsgType;
import org.hzero.boot.message.entity.Message;
import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JobHandler("DEMO-47359")
public class DemoJob implements IJobHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoJob.class);

    // for sending through feishu
    private static final String SERVER_CODE = "FEIYU";
    private static final String MESSAGE_TEMPLATE_CODE = "DEMO-47359";
    private static final String LANGUAGE = "zh_CN";

    // from request definition
    private static final String RECEIVER_EMAIL_PARAM = "email";
    private static final String SENDER_ID_PARAM = "empId";

    private static final String MESSAGE_EMAIL_ARG = "empEmail";
    private static final String MESSAGE_ID_ARG = "empId";
    private static final String MESSAGE_NAME_ARG = "empName";

    private final MessageClient client;
    private final UserService userService;

    @Autowired
    public DemoJob(MessageClient client, UserService userService) {
        this.client = client;
        this.userService = userService;
    }

    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {

        tool.info("Executing Task");

        // no null check required because of default value
        String receiverEmail = map.get(MessageConstant.RECEIVER_EMAIL_PARAM);
        String senderEmpId = map.get(MessageConstant.SENDER_ID_PARAM);

        List<Map<String, String>> flyBookUserList = new ArrayList<>();

        Map<String, String> flyBookUserMap = new HashMap<>();
        flyBookUserMap.put("email", receiverEmail);

        flyBookUserList.add(flyBookUserMap);

        Map<String, Object> args = new HashMap<>();

        User sender = userService.detail(tool.getBelongTenantId(), Long.valueOf(senderEmpId));
        args.put(MessageConstant.MESSAGE_EMAIL_ARG, sender.getEmail());
        args.put(MessageConstant.MESSAGE_ID_ARG, sender.getId());
        args.put(MessageConstant.MESSAGE_NAME_ARG, sender.getEmployeeName());

        Message res = client.sendFlyBook(
                tool.getBelongTenantId(),
                MessageConstant.SERVER_CODE,
                MessageConstant.MESSAGE_TEMPLATE_CODE,
                FlyBookMsgType.TEXT,
                MessageConstant.LANGUAGE,
                flyBookUserList,
                args
        );

        if (res.getSendFlag() == 1) {
            tool.info("Task has been executed");
            return ReturnT.SUCCESS;
        } else {
            tool.info("Task Failed to execute");
            return ReturnT.FAILURE;
        }
    }
}
