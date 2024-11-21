package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.ExternalDTO;
import com.hand.demo.app.service.ExternalService;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;
import org.hzero.core.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * (External Controller)表控制层
 *
 * @author
 * @since 2024-11-04 10:16:08
 */

@RestController("ExternalController.v1")
@RequestMapping("v1/{organizationId}")
public class ExternalController extends BaseController {
    @Autowired
    private ExternalService externalService;

    @ApiOperation(value = "Get Header Detail")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/external/header-detail")
    public ResponseEntity<ResponsePayloadDTO> getHeaderDetail(@PathVariable Long organizationId, ExternalDTO externalDTO) {
        return externalService.invokeInterface(externalDTO);
    }
}
