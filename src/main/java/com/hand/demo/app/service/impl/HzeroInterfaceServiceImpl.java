package com.hand.demo.app.service.impl;

import com.hand.demo.app.service.HzeroInterfaceService;
import com.hand.demo.domain.dto.ExternalInterfaceDTO;
import org.hzero.boot.interfaces.sdk.dto.InterfaceDTO;
import org.hzero.boot.interfaces.sdk.dto.RequestPayloadDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;
import org.hzero.boot.interfaces.sdk.invoke.InterfaceInvokeSdk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class HzeroInterfaceServiceImpl implements HzeroInterfaceService {
    @Autowired
    private InterfaceInvokeSdk interfaceInvokeSdk;

    @Override
    public ResponsePayloadDTO invokeInterface(ExternalInterfaceDTO externalInterfaceDTO, String param){
        RequestPayloadDTO requestPayloadDTO = new RequestPayloadDTO();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("text", param);
        requestPayloadDTO.setRequestParamMap(paramMap);
        return  interfaceInvokeSdk.invoke(externalInterfaceDTO.getNamespace(), externalInterfaceDTO.getServerCode(), externalInterfaceDTO.getInterfaceCode(), requestPayloadDTO);
    }

    @Override
    public ResponsePayloadDTO invokeCalculator(String paramXml, ExternalInterfaceDTO externalInterfaceDTO){
        RequestPayloadDTO requestPayloadDTO = new RequestPayloadDTO();
        requestPayloadDTO.setPayload(paramXml);
        requestPayloadDTO.setMediaType("application/soap+xml");

        return interfaceInvokeSdk.invoke(externalInterfaceDTO.getNamespace(), externalInterfaceDTO.getServerCode(), externalInterfaceDTO.getInterfaceCode(), requestPayloadDTO);
    }

//    @Override
//    public ResponsePayloadDTO invokeCalculatorRequestParam(String paramXml, ExternalInterfaceDTO externalInterfaceDTO){
//        RequestPayloadDTO requestPayloadDTO = new RequestPayloadDTO();
//        requestPayloadDTO.setPayload(paramXml);
//        requestPayloadDTO.setMediaType("application/soap+xml");
//
//        return interfaceInvokeSdk.invoke(externalInterfaceDTO.getNamespace(), externalInterfaceDTO.getServerCode(), externalInterfaceDTO.getInterfaceCode(), requestPayloadDTO);
//    }
}
