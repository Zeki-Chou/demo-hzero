package com.hand.demo.api.controller.v1;

import com.hand.demo.app.service.MessageService;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.checkerframework.checker.units.qual.A;
import org.hzero.boot.message.entity.Message;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("messageController.v1" )
@RequestMapping("/v1/{organizationId}/message" )
public class MessageController extends BaseController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @ApiOperation(value = "SendMessage")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("sendMassage")
    public ResponseEntity<Message> sendMessage(@PathVariable("organizationId") Long organizationId,
                                               @RequestParam Long receiverId,
                                               @RequestParam List<String> messageInput){
        Message message = messageService.sendMessage(receiverId, organizationId, messageInput);
        return Results.success(message);
    }

    @ApiOperation(value = "SendEmail")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("sendEmail")
    public ResponseEntity<String> sendEmail(@PathVariable("organizationId") Long organizationId,
                                               @RequestParam String email,
                                               @RequestParam String contextJSON){
        Message message = messageService.sendEmail(organizationId, email, contextJSON);
        return Results.success("Parameter: " +contextJSON + message.getContent());
    }

    @ApiOperation(value = "sendFeshu")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("sendFeshu")
    public ResponseEntity<String> sendFeshu(@PathVariable("organizationId") Long organizationId,
                                            @RequestParam String email,
                                            @RequestParam String contextJSON){
        Message message = messageService.sendEmail(organizationId, email, contextJSON);
        return Results.success("Parameter: " +contextJSON + message.getContent());
    }
}