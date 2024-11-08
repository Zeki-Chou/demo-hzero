package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.app.service.MessageService;
import com.hand.demo.app.service.UserService;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.UserRepository;
import io.choerodon.core.exception.CommonException;
import org.hzero.boot.message.MessageClient;
import org.hzero.boot.message.entity.FlyBookMsgType;
import org.hzero.boot.message.entity.Message;
import org.hzero.boot.message.entity.Receiver;
import org.hzero.core.base.BaseAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MessageServiceImpl extends BaseAppService implements MessageService {
    private final MessageClient messageClient;
    private final UserService userService;

    @Autowired
    public MessageServiceImpl(MessageClient messageClient, UserService userService, UserRepository userRepository) {
        this.messageClient = messageClient;
        this.userService = userService;
    }

    @Override
    public Message flybookMessage(Long organizationId, Long userId) {

        String serverCode = "FEIYU";
        String messageTemplateCode = "FEISHU_47357";
        FlyBookMsgType msgType = FlyBookMsgType.TEXT;
        String lang = "en_US";

        List<Map<String, String>> flyBookUserIdList = new ArrayList<>();
        Map<String, String> userMap = new HashMap<>();
        userMap.put("email", "shaoqin.zhou@hand-china.com");
        flyBookUserIdList.add(userMap);

        User user = userService.detail(organizationId, userId);

        Map<String, Object> args = new HashMap<>();
        args.put("email", user.getEmail());
        args.put("number", user.getEmployeeNumber());
        args.put("name", user.getEmployeeName());

        return messageClient.sendFlyBook(organizationId, serverCode, messageTemplateCode, msgType, lang, flyBookUserIdList, args);
    }



    @Override
    public Message webMessage(List<String> params, Long receiverId, Long organizationId) {

        String messageTemplateCode = "DEMO_47357";
        String lang = "en_US";

        Receiver receiver = new Receiver()
                .setUserId(receiverId)
                .setTargetUserTenantId(organizationId);

        Map<String, String> paramMap = new HashMap<>();

        if (params != null && !params.isEmpty()) {

            paramMap.put("msg", params.get(0));
            paramMap.put("msg2", (params.size() > 1 && params.get(1) != null) ? params.get(1) : "default");

        }
        return messageClient.sendWebMessage(organizationId, messageTemplateCode, lang, Collections.singletonList(receiver), paramMap);
    }


    @Override
    public Message emailMessage(String contextJson, String email, Long organizationId) {

        String serverCode = "DEMO_47357";
        String messageTemplateCode = "DEMO_47357";
        String language = "en_US";

        List<Receiver> receivers = Collections.singletonList(
                new Receiver().setEmail(email)
        );

        if (receivers == null || receivers.isEmpty()) {
            throw new IllegalArgumentException("Recipient email can't be null");
        }


        Map<String, String> parameters;
        try {
            parameters = JSON.parseObject(contextJson, Map.class);
        } catch (Exception ex) {
            throw new CommonException("Error: Unable to parse the JSON context", ex);
        }

        return messageClient.sendEmail(organizationId, serverCode, messageTemplateCode, language, receivers, parameters, null);
    }
}
