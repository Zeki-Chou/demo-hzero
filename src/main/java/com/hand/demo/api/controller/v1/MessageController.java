package com.hand.demo.api.controller.v1;

import com.hand.demo.app.service.MessageService;
import org.hzero.core.util.Results;
import org.hzero.core.base.BaseController;
import com.hand.demo.app.service.TaskService;
import com.hand.demo.domain.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;

import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * 任务表 管理 API
 *
 * @author allan.sugianto@hand-global.com 2024-10-17 14:34:10
 */
@RestController("messageController.v1" )
@RequestMapping("/v1/{organizationId}/messages" )
public class MessageController extends BaseController {

    private final MessageService messageService;
    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @ApiOperation(value = "Send Station")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/station")
    public ResponseEntity<String> sendStation(@PathVariable("organizationId") Long organizationId, @RequestParam List<String> messages, Long receiverId) {
        messageService.checkAndSendStationMessage(messages, receiverId, organizationId);
        return Results.success("message broadcasted");
    }

    @ApiOperation(value = "Send Email")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(String contextJson, String emailAddress, @PathVariable("organizationId")Long organizationId) {
        messageService.checkAndSendEmailMessage(contextJson, emailAddress, organizationId);
        return Results.success("message broadcasted");
    }

}
