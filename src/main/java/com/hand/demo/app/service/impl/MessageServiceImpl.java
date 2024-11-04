package com.hand.demo.app.service.impl;

import com.hand.demo.app.service.MessageService;
import com.hand.demo.domain.entity.MessageFormat;
import org.codehaus.jackson.map.ObjectMapper;
import org.hzero.boot.message.MessageClient;
import org.hzero.boot.message.entity.Message;
import org.hzero.boot.message.entity.Receiver;
import org.hzero.core.base.BaseAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Service
public class MessageServiceImpl extends BaseAppService implements MessageService {

    private final MessageClient client;

    @Autowired
    public MessageServiceImpl(MessageClient client) {
        this.client = client;
    }

    @Override
    public void checkAndSendStationMessage(List<String> messages, Long receiverId, Long organizationId) {
        // initialise variable
        Map<String, String> msgArgs = new HashMap<>();
        String templateCode = "DEMO-47359";

        Receiver receiver = new Receiver();
        receiver.setTargetUserTenantId(organizationId);
        receiver.setUserId(receiverId);

        List<Receiver> receivers = new ArrayList<>();
        receivers.add(receiver);

        Map<String, String> params = new HashMap<>();


        if (messages == null || messages.isEmpty()) {
            // generate template code
            Message message = client.sendWebMessage(organizationId, "DEMO-47359", receivers, params);
            params.put("msg1", "${msg1}");
            params.put("msg2", "${msg2}");
        } else {
            for (int i = 0; i < messages.size(); i++) {
                System.out.println(messages.get(i));
                params.put("msg" + (i+1), messages.get(i));
            }

            if (messages.size() < 2) {
                params.put("msg" + 2, "${msg2}");
            }
            Message message = client.sendWebMessage(organizationId,"DEMO-47359", receivers, params);
        }

    }

    @Override
    public void checkAndSendEmailMessage(String contextJson, String emailAddress, Long organizationId) {
        Receiver receiver = new Receiver();
        receiver.setTargetUserTenantId(organizationId);
        receiver.setEmail(emailAddress);

        List<Receiver> receivers = new ArrayList<>();
        receivers.add(receiver);

        Map<String, String> params = new HashMap<>();

        if (contextJson == null) {
            // generate template code
            Message message = client.sendWebMessage(organizationId, "DEMO-47359", receivers, params);
            params.put("msg1", "${msg1}");
            params.put("msg2", "${msg2}");
        } else {
            MessageFormat format = JSON.parseObject(contextJson, MessageFormat.class);
            if (format.getMsg1() == null) {
                params.put("msg1", "${msg1}");
            } else {
                params.put("msg1", format.getMsg1());
            }

            if (format.getMsg2() == null) {
                params.put("msg2", "${msg2}");
            } else {
                params.put("msg2", format.getMsg2());
            }
        }
        client.sendEmail(organizationId, "DEMO-47359", "DEMO-47359", "zh_CN", receivers, params);
    }

    @Override
    public void checkAndSendFeishuMessage(String contextJson, String emailAddress, Long organizationId) {

    }
}
