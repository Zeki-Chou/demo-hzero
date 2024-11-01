package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.app.service.MessageService;
import com.hand.demo.app.service.UserService;
import com.hand.demo.domain.entity.User;
import io.choerodon.core.exception.CommonException;
import org.hzero.boot.message.MessageClient;
import org.hzero.boot.message.entity.FlyBookMsgType;
import org.hzero.boot.message.entity.Message;
import org.hzero.boot.message.entity.Receiver;
import org.hzero.core.base.BaseAppService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MessageServiceImpl extends BaseAppService implements MessageService {
    private final MessageClient messageClient;
    private final UserService userService;

    public MessageServiceImpl(MessageClient messageClient, UserService userService) {
        this.messageClient = messageClient;
        this.userService = userService;
    }

    @Override
    public Message sendMessage(Long receiverId, Long organizationId, List<String> messages){
        String messageTemplateCode = "DEMO-47361";
        String language = "en_US";
        Receiver receiver = new Receiver();
        receiver.setUserId(receiverId);
        receiver.setTargetUserTenantId(organizationId);
        List<Receiver> listReceiver = new LinkedList<>();
        listReceiver.add(receiver);

        Map<String, String> listMessages = new HashMap<>(2);
        if(messages != null || !messages.isEmpty()){
            listMessages.put("msg", messages.get(0));
            if(messages.get(1) == null){
                listMessages.put("msg2", "default");
            }else {
                listMessages.put("msg2", messages.get(1));
            }
        }

        Message message = messageClient.sendWebMessage(organizationId, messageTemplateCode, language, listReceiver, listMessages);
        return message;
    }
    @Override
    public Message sendEmail(Long organizationId, String email, String messages){
        String messageTemplateCode = "DEMO-47361";
        String serverCode = "DEMO_47361";
        String language = "en_US";

        Receiver receiver = new Receiver();
        receiver.setEmail(email);
        receiver.setTargetUserTenantId(organizationId);

        List<Receiver> listReceiver = new LinkedList<>();
        listReceiver.add(receiver);

        Map<String, String> messageMap;
        try{
            messageMap = JSON.parseObject(messages, Map.class);
        }catch (Exception ex){
            throw new CommonException("Failed to Parsing");
        }

        Message message = messageClient.sendEmail(organizationId, serverCode, messageTemplateCode, language, listReceiver, messageMap, null);
        return message;
    }

    @Override
    public Message sendFeshu(Long organizationId, long userId){
        String messageTemplateCode = "DEMO_47361_FLYBOOK";
        String serverCode = "FEIYU";
        String language = "en_US";
        FlyBookMsgType msgType = FlyBookMsgType.TEXT;


        List<Map<String, String>> flyBookUserIdList = new LinkedList<>();
        Map<String, String> receive = new HashMap<>();
        receive.put("email", "shaoqin.zhou@hand-china.com");
        flyBookUserIdList.add(receive);


        User user = userService.detail(organizationId, userId);
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("name", user.getEmployeeName());
        messageMap.put("empNumber", user.getEmployeeNumber());
        messageMap.put("email", user.getEmail());

        Message message = messageClient.sendFlyBook(organizationId, serverCode, messageTemplateCode, msgType, language, flyBookUserIdList, messageMap);

        return message;
    }


}
