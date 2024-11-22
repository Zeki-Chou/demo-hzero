package com.hand.demo.app.service.impl;

import com.hand.demo.app.service.MessageService;
import com.hand.demo.domain.entity.MessageFormat;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.UserRepository;
import org.hzero.boot.message.MessageClient;
import org.hzero.boot.message.entity.FlyBookMsgType;
import org.hzero.boot.message.entity.Message;
import org.hzero.boot.message.entity.Receiver;
import org.hzero.core.base.BaseAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageServiceImpl extends BaseAppService implements MessageService {

    private final MessageClient client;
    private final UserRepository userRepository;

    @Autowired
    public MessageServiceImpl(MessageClient client, UserRepository userRepository) {
        this.client = client;
        this.userRepository = userRepository;
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
            params.put("msg1", "${msg1}");
            params.put("msg2", "${msg2}");
            Message message = client.sendWebMessage(organizationId, "DEMO-47359", receivers, params);
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
    public Message checkAndSendFeishuMessage(String contextJson, String emailAddress, Long organizationId) {
        String serverCode = "FEIYU";
        String messageTemplateCode = "DEMO-47359";
        String lang ="en_US";

        List<Map<String, String>> flyBookUserIdList = new ArrayList<>();
        Map<String, String> userMap = new HashMap<>();
        userMap.put("email", emailAddress);
        flyBookUserIdList.add(userMap);

        FlyBookMsgType textMessage = FlyBookMsgType.TEXT;
        Map<String, Object> args = new HashMap<>();

        User user = userRepository.selectByPrimaryKey(2280L);
        args.put("email", user.getEmail());
        args.put("id", user.getEmployeeNumber());
        args.put("name", user.getEmployeeName());

        return client.sendFlyBook(organizationId, serverCode, messageTemplateCode, textMessage, lang , flyBookUserIdList, args);
    }
}
