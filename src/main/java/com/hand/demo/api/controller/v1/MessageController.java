package com.hand.demo.api.controller.v1;

import com.hand.demo.app.service.impl.MessageServiceImpl;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.message.entity.Message;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("messageController.v1" )
@RequestMapping("/v1/{organizationId}/messages" )
public class MessageController extends BaseController {
   @Autowired
   MessageServiceImpl messageService;

   Logger logger
           = LoggerFactory.getLogger(MessageController.class);

   @ApiOperation(value = "SEND WEB MESSAGE")
   @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
   @PostMapping("/web-message")
   public ResponseEntity<Message> webMessage(@RequestParam List<String> params, @RequestParam Long receiverId, @PathVariable Long organizationId) {
      Message message = messageService.webMessage(params, receiverId, organizationId);
      return Results.success(message);
   }

   @ApiOperation(value = "SEND EMAIL MESSAGE")
   @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
   @PostMapping("/email-message")
   public ResponseEntity<String> emailMessage(@RequestParam String contextJson, @RequestParam String email,@PathVariable Long organizationId) {
      Message message = messageService.sendMail(contextJson, email ,organizationId);
      return Results.success("params" + contextJson + "Content: " + message.getContent());
   }

   @ApiOperation(value = "SEND FESIHU MESSAGE")
   @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
   @PostMapping("/feishu-message")
   public ResponseEntity<String> feishuMessage(@RequestParam Long userId, @RequestParam String email ,@PathVariable Long organizationId) {
      Message message = messageService.sendFeishu(userId, organizationId, email);
      return Results.success("params" + userId + "Content: " + message.getContent());
   }

}
