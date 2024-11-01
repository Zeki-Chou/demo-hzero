package com.hand.demo.api.controller.v1;

import com.hand.demo.app.service.MessageService;
import com.hand.demo.api.dto.MessageRequest;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.hzero.boot.message.entity.Message;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController("messageController.v1")
@RequestMapping("/v1/{organizationId}/message")
@AllArgsConstructor
public class MessageController {

    private MessageService messageService;

    @ApiOperation(value = "send-message-to-station")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/send-station/{receiverId}")
    public Message sendStationMessage(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable("receiverId") Long receiverId,
            @RequestParam List<String> messages
    ) {
        MessageRequest request = MessageRequest.builder()
                .organizationId(organizationId)
                .receiverId(receiverId)
                .messages(messages)
                .build();

        return messageService.sendMessageToStation(request);
    }

    @ApiOperation(value = "send-message-to-email")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/send-email")
    public Message sendEmailMessage(
        @PathVariable("organizationId") Long organizationId,
        @RequestParam String contextJson
    ) {
        return messageService.sendMessageToEmail(organizationId, contextJson);
    }
}

