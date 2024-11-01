package com.hand.demo.app.service;

import org.hzero.boot.message.entity.Message;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface MessageService {
    Message webMessage(List<String> params, Long receiverId, Long organizationId);
    Message sendMail(String contextJson, String email , Long organizationId);
    Message sendFeishu(Long userId, Long organizationId, String email);
}
