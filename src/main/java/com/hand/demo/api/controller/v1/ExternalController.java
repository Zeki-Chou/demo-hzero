package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.ExternalRequestDTO;
import com.hand.demo.app.service.impl.ExternalServiceImpl;
import com.hand.demo.config.SwaggerTags;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.hzero.boot.interfaces.sdk.dto.InterfaceDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = SwaggerTags.EXTERNAL)
@RestController("externalController.v1")
@RequestMapping("/v1/{organizationId}/external")
public class ExternalController {
    @Autowired
    ExternalServiceImpl externalService;

    @ApiOperation(value = "CALL EXTERNAL")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/call")
    public ResponsePayloadDTO call (String jsonString,
                                   ExternalRequestDTO externalRequestDTO) {
        return externalService.invokeInterface(jsonString, externalRequestDTO);
    }

    @ApiOperation(value = "CALL EXTERNAL XML")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/call-xml")
    public ResponsePayloadDTO callXml (@RequestBody String requestXML,
                                    ExternalRequestDTO externalRequestDTO) {
        return externalService.invokeXML(requestXML, externalRequestDTO);
    }
}
