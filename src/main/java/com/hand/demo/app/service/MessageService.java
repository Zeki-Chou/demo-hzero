package com.hand.demo.app.service;

import org.hzero.boot.message.entity.Message;


import java.util.List;
import java.util.Map;

public interface MessageService {
    Message webMessage(List<String> params, Long receiverId, Long organizationId);

    Message email(String contextJson, String emailAddress, Long organizationId);
    Message flybook(Long employeeId, String email, Long organizationId);
}
