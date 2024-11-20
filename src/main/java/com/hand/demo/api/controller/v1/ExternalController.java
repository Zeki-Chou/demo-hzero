package com.hand.demo.api.controller.v1;

import com.hand.demo.api.controller.dto.CalculatorDTO;
import com.hand.demo.api.controller.dto.HeaderExternalDTO;
import com.hand.demo.api.controller.dto.TranslationDTO;
import com.hand.demo.app.service.ExternalService;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("externalController.v1")
@RequestMapping("/v1/{organizationId}/external")
public class ExternalController extends BaseController {
    private final ExternalService service;

    public ExternalController(ExternalService service) {
        this.service = service;
    }

    @ApiOperation(value = "translate from external")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/translate")
    public ResponseEntity<ResponsePayloadDTO> translate(TranslationDTO dto) {
        return Results.success(service.translateTextExternal(dto));
    }

    @ApiOperation(value = "add two numbers")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/add")
    public ResponseEntity<ResponsePayloadDTO> add(CalculatorDTO dto) {
        return Results.success(service.add(dto));
    }

    @ApiOperation(value = "invoice header detail")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/invoice-header/detail")
    public ResponseEntity<ResponsePayloadDTO> invHeaderDetail(HeaderExternalDTO dto, @PathVariable Long organizationId) {
        return Results.success(service.getInvoiceHeaderDetailExternal(organizationId, dto));
    }

}
