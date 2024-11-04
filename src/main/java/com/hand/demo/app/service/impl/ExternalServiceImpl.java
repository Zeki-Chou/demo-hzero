package com.hand.demo.app.service.impl;

import com.hand.demo.api.controller.dto.CalculatorDTO;
import com.hand.demo.api.controller.dto.TranslationDTO;
import com.hand.demo.app.service.ExternalService;
import org.hzero.boot.interfaces.sdk.dto.RequestPayloadDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;
import org.hzero.boot.interfaces.sdk.invoke.InterfaceInvokeSdk;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ExternalServiceImpl implements ExternalService {
    private final InterfaceInvokeSdk interfaceInvokeSdk;

    public ExternalServiceImpl(InterfaceInvokeSdk interfaceInvokeSdk) {
        this.interfaceInvokeSdk = interfaceInvokeSdk;
    }

    @Override
    public ResponsePayloadDTO translateTextExternal(TranslationDTO dto) {
        RequestPayloadDTO requestPayload = new RequestPayloadDTO();
        Map<String, String> requestParamsMap = new HashMap<>();
        requestParamsMap.put("text", dto.getText());
        requestPayload.setRequestParamMap(requestParamsMap);
        return interfaceInvokeSdk.invoke(dto.getNameSpace(), dto.getServerCode(), dto.getInterfaceCode(), requestPayload);
    }

    @Override
    public ResponsePayloadDTO add(CalculatorDTO dto) {
        RequestPayloadDTO requestPayload = new RequestPayloadDTO();
        Map<String, String> requestParamsMap = new HashMap<>();

        String payloadNumber = String.format(
                "<tem:intA>%d</tem:intA>" +
                        "<tem:intB>%d</tem:intB>", dto.getNum1(), dto.getNum2());

        requestPayload.setPayload(payloadNumber);
        requestPayload.setMediaType("application/soap+xml");
        requestPayload.setRequestParamMap(requestParamsMap);
        return interfaceInvokeSdk.invoke(dto.getNameSpace(), dto.getServerCode(), dto.getInterfaceCode(), requestPayload);
    }
}
