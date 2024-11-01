package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.app.service.MessageService;
import com.hand.demo.app.service.UserService;
import com.hand.demo.api.dto.MessageRequest;
import com.hand.demo.domain.entity.User;
import io.choerodon.core.exception.CommonException;
import lombok.AllArgsConstructor;
import org.hzero.boot.message.MessageClient;
import org.hzero.boot.message.entity.FlyBookMsgType;
import org.hzero.boot.message.entity.Message;
import org.hzero.boot.message.entity.Receiver;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class MessageServiceImpl implements MessageService {

    private MessageClient messageClient;
    private UserService userService;

    private final String applicationId = "cli_a631a2493172100e";

    @Override
    public Message sendMessageToStation(MessageRequest request) {
        System.out.println(request.getMessages());
        String templateCode = "DEMO-47355";
        String lang = "en_US";
        List<Receiver> receivers = Collections.singletonList(new Receiver().setUserId(request.getReceiverId()).setTargetUserTenantId(request.getOrganizationId()));
        Map<String, String> message = new HashMap<>(2);

        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            return messageClient.sendWebMessage(request.getOrganizationId(), templateCode, lang, receivers, message);
        }

        if (request.getMessages().size() > 0 && request.getMessages().get(0) != null) {
            message.put("msg", request.getMessages().get(0));
        }

        if (request.getMessages().size() > 1 && request.getMessages().get(1) != null) {
            message.put("msg2", request.getMessages().get(1));
        } else {
            message.put("msg2", "default");
        }

        return messageClient.sendWebMessage(request.getOrganizationId(), templateCode, lang, receivers, message);
    }

    @Override
    public Message sendMessageToEmail(Long organizationId, String contextJson) {
        String templateCode = "DEMO-47355";
        String lang = "en_US";

        Receiver receiver = new Receiver();
        receiver.setEmail("shaoqin.zhou@hand-china.com");

        List<Receiver> receiverList = Collections.singletonList(receiver);

        Map<String, String> paramMap;
        try {
            paramMap = JSON.parseObject(contextJson, Map.class);
        } catch (Exception e) {
            throw new CommonException("Failed parsing JSON", e);
        }

        return messageClient.sendEmail(organizationId, templateCode, templateCode, lang, receiverList, paramMap, null);

    }

    @Override
    public Message sendMessageToFlyBook(Long userId) {
        String templateCode = "DEMO-47355-FLYBOOK";
        String lang = "en_US";
        String serverCode = "FEIYU";
        FlyBookMsgType messageType = FlyBookMsgType.TEXT;
        List<Map<String, String>> flyBookUserListId = new ArrayList<>();

        Map<String, String> map = new HashMap<>();
        map.put("email", "shaoqin.zhou@hand-china.com");
        flyBookUserListId.add(map);

        long tenantId = 0L;
        User user = userService.detail(tenantId, userId);

        Map<String, Object> messageContent = new HashMap<>();
        messageContent.put("name", user.getEmployeeName());
        messageContent.put("empNumber", user.getEmployeeNumber());
        messageContent.put("email", user.getEmail());
        return messageClient.sendFlyBook(
                tenantId,
                serverCode,
                templateCode,
                messageType,
                lang,
                flyBookUserListId,
                messageContent
        );
    }

}
