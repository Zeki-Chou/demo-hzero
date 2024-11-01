package com.hand.demo.api.controller.v1;

import com.hand.demo.app.service.impl.MessageServiceImpl;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.message.entity.Message;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
/**
 * Message API
 *
 * @author lareza.farhan@hand-global.com 2024-10-17 14:07:13
 */
@RestController("messageController.v1" )
@RequestMapping("/v1/{organizationId}/messages" )
public class MessageController {
    @Autowired
    MessageServiceImpl messageServiceImpl;

    @ApiOperation(value = "Web Message")
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @PostMapping("/web-message")
    public ResponseEntity<Message> webMessage(@RequestParam List<String> params, @RequestParam Long receiverId, @PathVariable Long organizationId) {
        Message message = messageServiceImpl.webMessage(params,receiverId,organizationId);
        return Results.success(message);
    }

    @ApiOperation(value = "Email")
    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @PostMapping("/email")
    public ResponseEntity<String> email(@RequestParam String contextJson,@RequestParam String emailAddress, @PathVariable Long organizationId) {
        Message message = messageServiceImpl.email(emailAddress,contextJson,organizationId);
        return Results.success( "parameters:" + contextJson + "; Content:" + message.getContent());
    }
}
