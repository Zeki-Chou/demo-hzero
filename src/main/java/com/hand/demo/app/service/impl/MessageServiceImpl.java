package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.app.service.MessageService;
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
    @Autowired
    private MessageClient messageClient;
    @Autowired
    private UserRepository userRepository;

    @Override
    public Message webMessage(List<String> params, Long receiverId, Long organizationId) {
        String messageTempCode = "DEMO-47358";
        String lang = "en_US";
        Receiver receiver = new Receiver().setUserId(receiverId).setTargetUserTenantId(organizationId);
        Map<String, String> paramMap = new HashMap<>(2);
        if(params != null && !params.isEmpty()) {
            paramMap.put("msg", params.get(0));
            paramMap.put("msg2", params.size() > 1 ? params.get(1) : "default");
        }

        List<Receiver> receivers = new ArrayList<>();
        receivers.add(receiver);

        return messageClient.sendWebMessage(organizationId, messageTempCode, lang, receivers, paramMap);
    }

    @Override
    public Message sendMail(String contextJson, String email,Long organizationId) {
        String serverCode = "DEMO_47358";
        String messageTemplateCode = "DEMO-47358";
        String lang ="en_US";
        Receiver receiver = new Receiver();
        receiver.setEmail(email);
        List<Receiver> receivers = new ArrayList<>();
        receivers.add(receiver);
        Map<String, String> args = new HashMap<>();
        try {
            args = JSON.parseObject(contextJson, Map.class);
        } catch (Exception e) {
            throw new CommonException("Json Parse Fail");
        }
        return messageClient.sendEmail(organizationId, serverCode, messageTemplateCode, lang, receivers, args);
    }

    @Override
    public Message sendFeishu(Long userId, Long organizationId, String email) {
        String serverCode = "FEIYU";
        String messageTemplateCode = "DEMO-47358-NEW";
        String lang ="en_US";

        List<Map<String, String>> flyBookUserIdList = new ArrayList<>();
        Map<String, String> userMap = new HashMap<>();
        userMap.put("email", email);
        flyBookUserIdList.add(userMap);

        FlyBookMsgType textMessage = FlyBookMsgType.TEXT;
        Map<String, Object> args = new HashMap<>();

        User user = userRepository.selectByPrimaryKey(userId);
        args.put("email", user.getEmail());
        args.put("id", user.getEmployeeNumber());
        args.put("name", user.getEmployeeName());

        return messageClient.sendFlyBook(organizationId, serverCode, messageTemplateCode, textMessage, lang , flyBookUserIdList, args);
    }


}
