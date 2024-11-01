package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.ExternalInterfaceDTO;
import com.hand.demo.app.service.ExternalInterfaceService;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController("externalInterfaceController.v1" )
@RequestMapping("/v1/{organizationId}/interface" )
public class ExternalInterfaceController {

    @Autowired
    ExternalInterfaceService externalInterfaceService;

    @ApiOperation(value = "Invoke Interface")
    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @PostMapping
    public ResponseEntity<ResponsePayloadDTO> invokeInterface(@PathVariable Long organizationId, @RequestBody ExternalInterfaceDTO externalInterfaceDTO){
        ResponsePayloadDTO responsePayloadDTO = externalInterfaceService.invokeInterface(organizationId,externalInterfaceDTO);
        return Results.success(responsePayloadDTO);
    }
}
