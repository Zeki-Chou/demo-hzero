package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.ExternalInterfaceDTO;
import com.hand.demo.app.service.ExternalInterfaceService;
import org.hzero.boot.interfaces.sdk.dto.RequestPayloadDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;
import org.hzero.boot.interfaces.sdk.invoke.InterfaceInvokeSdk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class ExternalInterfaceServiceImpl implements ExternalInterfaceService {

    @Autowired
    private InterfaceInvokeSdk interfaceInvokeSdk;

    @Override
    public ResponsePayloadDTO invokeInterface(Long organizationId, ExternalInterfaceDTO externalInterfaceDTO){
        RequestPayloadDTO requestPayloadDTO = new RequestPayloadDTO();
        requestPayloadDTO.setRequestParamMap(externalInterfaceDTO.getRequestMap());
        requestPayloadDTO.setPayload(externalInterfaceDTO.getPayload());
        requestPayloadDTO.setMediaType(externalInterfaceDTO.getMediaType());
        return interfaceInvokeSdk.invoke(externalInterfaceDTO.getNamespace(), externalInterfaceDTO.getServerCode(),
                externalInterfaceDTO.getInterfaceCode(), requestPayloadDTO);
    }
}
