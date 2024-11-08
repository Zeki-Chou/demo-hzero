package com.hand.demo.api.controller.v1;

import com.hand.demo.app.service.MessageService;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.hzero.boot.message.entity.Message;
import org.hzero.boot.message.entity.Receiver;
import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("messageController.v1")
@RequestMapping("/v1/{organizationId}/mess")
@AllArgsConstructor
public class MessageController {

    private MessageService messageService;

    @ApiOperation(value = "Web Message")
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @GetMapping("/web")
    public Message webMessage (@RequestParam List<String> params, @RequestParam Long receiverId, @PathVariable Long organizationId){
        return messageService.webMessage(params, receiverId, organizationId);
    }

    @ApiOperation(value = "Email Message")
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @GetMapping("/email")
    public Message emailMessage (@RequestParam String contextJson, @RequestParam String email, @PathVariable Long organizationId){

        return messageService.emailMessage(contextJson, email, organizationId);
    }

//    @ApiOperation(value = "Test Hello")
//    @Permission(level = ResourceLevel.ORGANIZATION)
//    @GetMapping("/hello")
//    public String hello (@PathVariable("organizationId") Long organizationId){
//        return "hello world";
//    }

}
