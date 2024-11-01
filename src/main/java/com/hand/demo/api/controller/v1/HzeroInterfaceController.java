package com.hand.demo.api.controller.v1;

import com.hand.demo.app.service.HzeroInterfaceService;
import com.hand.demo.domain.dto.ExternalInterfaceDTO;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController("hzeroInterfaceController.v1")
@RequestMapping("/v1/{organizationId}/hzero-interface")
public class HzeroInterfaceController {
    @Autowired
    private HzeroInterfaceService hzeroInterfaceService;

    @ApiOperation(value = "external translation")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/translation")
    public ResponsePayloadDTO externalInterface(ExternalInterfaceDTO externalInterfaceDTO,
                                                @RequestParam String parameter) {
        return hzeroInterfaceService.invokeInterface(externalInterfaceDTO, parameter);
    }

    @ApiOperation(value = "external calculator")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/calculator")
    public ResponsePayloadDTO externalCalculator(ExternalInterfaceDTO externalInterfaceDTO,
                                                 @RequestBody String paramXml) {
        return hzeroInterfaceService.invokeCalculator(paramXml, externalInterfaceDTO);
    }
}
