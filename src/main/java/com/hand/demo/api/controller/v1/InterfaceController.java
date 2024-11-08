package com.hand.demo.api.controller.v1;

import com.hand.demo.app.service.InterfaceService;
import com.hand.demo.domain.entity.User;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.hzero.boot.interfaces.sdk.dto.InterfaceDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;
import org.springframework.web.bind.annotation.*;

@RestController("interfaceController.v1")
@RequestMapping("/v1/{organizationId}/interface")
@AllArgsConstructor
public class InterfaceController {

    private InterfaceService interfaceService;


    @ApiOperation(value = "Translate")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/translate")
    public ResponsePayloadDTO translate(
            @RequestParam String param,
            String namespace,
            String serverCode,
            String interfaceCode) {
        return interfaceService.translate(param, namespace, serverCode, interfaceCode);
    }

    @ApiOperation(value = "Calculate")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/calculate")
    public ResponsePayloadDTO calculate(
            @RequestBody String xmlString,
            @RequestParam String namespace,
            @RequestParam String serverCode,
            @RequestParam String interfaceCode) {

        return interfaceService.calculate(xmlString, namespace, serverCode, interfaceCode);
    }

}
