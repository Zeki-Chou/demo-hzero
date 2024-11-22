package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.CalculatorRequest;
import com.hand.demo.api.dto.RezaListHeaderRequestDTO;
import com.hand.demo.api.dto.TranslationRequest;
import com.hand.demo.api.dto.WithdrawWorkflowRequest;
import com.hand.demo.infra.feign.ExternalTranslationService;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.hzero.boot.interfaces.sdk.dto.RequestPayloadDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;
import org.hzero.core.util.Results;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("externalServiceController.v1")
@RequestMapping("/v1/{organizationId}")
@AllArgsConstructor
public class ExternalServiceController {
    private ExternalTranslationService translationService;

    @ApiOperation(value = "External service translate")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/translate")
    public ResponsePayloadDTO translate(
            @PathVariable Long organizationId,
            @RequestBody TranslationRequest request
    ) {
        return translationService.invokeInterface(request);
    }

    @ApiOperation(value = "External service add")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/calculator/add")
    public ResponsePayloadDTO calculate(
            @PathVariable Long organizationId,
            @RequestBody CalculatorRequest request
    ) {
        return translationService.invokeCalculatorInterface(request);
    }

    @ApiOperation(value = "External service reza header list")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/reza/header-list")
    public ResponsePayloadDTO invokeRezaInterface(
            @PathVariable Long organizationId,
            @RequestBody RezaListHeaderRequestDTO requestDTO) {
        return translationService.invokeRezaInterface(requestDTO);
    }
}
