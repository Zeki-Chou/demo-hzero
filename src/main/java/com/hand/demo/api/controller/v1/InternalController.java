package com.hand.demo.api.controller.v1;

import com.hand.demo.api.controller.dto.InternalUserDTO;
import com.hand.demo.app.service.InternalService;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("internalController.v1")
@RequestMapping("/v1/{organizationId}/internal")
public class InternalController extends BaseController {

    private final InternalService service;

    public InternalController(InternalService service) {
        this.service = service;
    }

    @ApiOperation(value = "save users")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/user-create")
    public ResponseEntity<List<InternalUserDTO>> save(@PathVariable Long organizationId, @RequestBody List<InternalUserDTO> users) {
        return Results.success(service.saveUser(users));
    }
}
