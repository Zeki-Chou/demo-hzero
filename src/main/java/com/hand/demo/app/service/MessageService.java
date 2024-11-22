package com.hand.demo.app.service;

import org.hzero.boot.message.entity.Message;

import java.util.List;

public interface MessageService {
    void checkAndSendStationMessage(List<String> messages, Long receiverId, Long organizationId);
    void checkAndSendEmailMessage(String contextJson, String emailAddress, Long organizationId);
    Message checkAndSendFeishuMessage(String contextJson, String emailAddress, Long organizationId);
}
