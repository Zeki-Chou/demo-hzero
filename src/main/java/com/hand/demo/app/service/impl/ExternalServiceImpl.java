package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.ExternalRequestDTO;
import com.hand.demo.app.service.ExternalService;
import io.swagger.models.auth.In;
import org.hzero.boot.interfaces.sdk.dto.InterfaceDTO;
import org.hzero.boot.interfaces.sdk.dto.RequestPayloadDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;
import org.hzero.boot.interfaces.sdk.invoke.InterfaceInvokeSdk;
import org.hzero.core.base.BaseAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ExternalServiceImpl extends BaseAppService implements ExternalService {
    @Autowired
    private InterfaceInvokeSdk interfaceInvokeSdk;

    @Override
    public  ResponsePayloadDTO invokeInterface(String jsonString, ExternalRequestDTO externalRequestDTO) {
        RequestPayloadDTO requestPayloadDTO = new RequestPayloadDTO();
        Map<String, String> map = new HashMap<>();
        map.put("text", jsonString);
        requestPayloadDTO.setRequestParamMap(map);
        return  interfaceInvokeSdk.invoke(externalRequestDTO.getNamespace(), externalRequestDTO.getServerCode(),
                externalRequestDTO.getInterfaceCode(), requestPayloadDTO);
    }

    @Override
    public ResponsePayloadDTO invokeXML(String requestXML, ExternalRequestDTO externalRequestDTO) {
        RequestPayloadDTO requestPayloadDTO = new RequestPayloadDTO();
        requestPayloadDTO.setPayload(requestXML);
        requestPayloadDTO.setMediaType("application/soap+xml");
        return interfaceInvokeSdk.invoke(externalRequestDTO.getNamespace(), externalRequestDTO.getServerCode(),
                externalRequestDTO.getInterfaceCode(), requestPayloadDTO);
    }
}
