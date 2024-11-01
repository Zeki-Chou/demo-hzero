package com.hand.demo.infra.feign.fallback;

import com.hand.demo.api.dto.CalculatorRequest;
import com.hand.demo.api.dto.TranslationRequest;
import com.hand.demo.infra.feign.ExternalTranslationService;
import lombok.AllArgsConstructor;
import org.hzero.boot.interfaces.sdk.dto.RequestPayloadDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;
import org.hzero.boot.interfaces.sdk.invoke.InterfaceInvokeSdk;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@AllArgsConstructor
public class ExternalTranslationServiceImpl implements ExternalTranslationService {
    private InterfaceInvokeSdk interfaceInvokeSdk;

    @Override
    public ResponsePayloadDTO invokeInterface(TranslationRequest request) {
        RequestPayloadDTO payload = new RequestPayloadDTO();
        payload.setRequestParamMap(request.getRequestParam());
        payload.setPayload(request.getJsonString());
        payload.setMediaType("application/json");
        return interfaceInvokeSdk.invoke(request.getNamespace(), request.getServerCode(), request.getInterfaceCode(), payload);
    }

    @Override
    public ResponsePayloadDTO invokeCalculatorInterface(CalculatorRequest request) {
        RequestPayloadDTO payload = new RequestPayloadDTO();
        payload.setPayload(request.getParam());
        payload.setMediaType("application/soap+xml");
        return interfaceInvokeSdk.invoke(request.getNamespace(), request.getServerCode(), request.getInterfaceCode(), payload);
    }
}
