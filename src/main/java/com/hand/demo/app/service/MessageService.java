package com.hand.demo.app.service;

import com.hand.demo.domain.entity.User;
import org.hzero.boot.message.entity.Message;

import java.util.List;
import java.util.Map;

public interface MessageService {

    Message sendMessage(Long receiverId, Long organizationId, List<String> messages);

    Message sendEmail(Long organizationId, String email, String messages);

    Message sendFeshu(Long organizationId, long userId);
}
