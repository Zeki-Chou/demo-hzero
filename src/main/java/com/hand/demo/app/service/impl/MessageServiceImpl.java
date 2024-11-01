package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;

import com.hand.demo.app.service.MessageService;
import com.hand.demo.domain.entity.User;
import com.hand.demo.domain.repository.TaskRepository;
import com.hand.demo.domain.repository.UserRepository;
import io.choerodon.core.exception.CommonException;

import org.hzero.boot.message.MessageClient;
import org.hzero.boot.message.entity.FlyBookMsgType;
import org.hzero.boot.message.entity.Message;
import org.hzero.boot.message.entity.Receiver;
import org.hzero.core.base.BaseAppService;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MessageServiceImpl  extends BaseAppService implements MessageService {
    private final UserRepository userRepository;
    private final MessageClient messageClient;

    @Autowired
    public MessageServiceImpl(MessageClient messageClient, UserRepository userRepository) {
        this.messageClient = messageClient;
        this.userRepository = userRepository;
    }

    @Override
    public Message webMessage(List<String> messages, Long receiverId, Long organizationId) {
        String messageTemplateCode = "DEMO-47360";
        String lang = "en_US";

        Map<String, String> args = new HashMap<>(2);
        if(messages != null && !messages.isEmpty()){
            args.put("msg",messages.get(0));
            args.put("msg2",messages.size()>1?messages.get(1):"default");
        }
        Receiver receiver = new Receiver().setUserId(receiverId).setTargetUserTenantId(organizationId);
        List<Receiver> receivers = new ArrayList<>();
        receivers.add(receiver);

        return messageClient.sendWebMessage(organizationId,messageTemplateCode,lang,receivers,args);
    }

    @Override
    public Message email(String contextJson, String emailAddress, Long organizationId) {
        String serverCode = "DEMO-47360";
        String messageTemplateCode = "DEMO-47360";
        String lang = "en_US";

        Receiver receiver = new Receiver().setEmail(emailAddress);
        List<Receiver> receivers = new ArrayList<>();
        receivers.add(receiver);

        Map<String, String> args = new HashMap<>(2);
        try {
            Map<String, String> mapJson = JSON.parseObject(contextJson, Map.class);
            args.put("msg",mapJson.getOrDefault("msg",""));
            args.put("msg2",mapJson.getOrDefault("msg2",""));
        } catch (Exception e) {
            throw new CommonException("Json Parse Fail");
        }
        return messageClient.sendEmail(organizationId, serverCode,messageTemplateCode, lang, receivers, args);

    }

    @Override
    public Message flybook(Long employeeId, String email, Long organizationId){
        String serverCode = "FEIYU";
        String messageTemplateCode = "USER-47360";
        String lang = "en_US";

        User user = userRepository.selectByPrimaryKey(employeeId);

        Map<String, Object> args = new HashMap<>();
        args.put("userName", user.getEmployeeName());
        args.put("userNumber", user.getEmployeeNumber());
        args.put("userEmail", user.getEmail());

        Map<String, String> userMap = new HashMap<>();
        userMap.put("email", email);
        List<Map<String, String>> flybookUsers= new ArrayList<>();
        flybookUsers.add(userMap);
        return messageClient.sendFlyBook(organizationId,serverCode,messageTemplateCode,FlyBookMsgType.TEXT,lang,flybookUsers,args);
    }
}
