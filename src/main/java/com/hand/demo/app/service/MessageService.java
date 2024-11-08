package com.hand.demo.app.service;


import org.hzero.boot.message.entity.FlyBookMsgType;
import org.hzero.boot.message.entity.Message;
import org.hzero.boot.message.entity.Receiver;

import java.util.List;
import java.util.Map;

public interface MessageService {

    Message webMessage (List<String> params, Long receiverId, Long organizationId);

    Message emailMessage (String contextJson, String email, Long organizationId);

    Message flybookMessage (Long organizationId, Long userId);

}


