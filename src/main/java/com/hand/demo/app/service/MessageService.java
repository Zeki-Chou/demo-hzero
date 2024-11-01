package com.hand.demo.app.service;

import com.hand.demo.api.dto.MessageRequest;
import org.hzero.boot.message.entity.Message;

public interface MessageService {
    Message sendMessageToStation(MessageRequest request);
    Message sendMessageToEmail(Long organizationId, String contextJson);
    Message sendMessageToFlyBook(Long userId);
}
