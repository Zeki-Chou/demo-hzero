package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.ExternalDTO;
import com.hand.demo.app.service.ExternalService;
import org.hzero.boot.interfaces.sdk.dto.RequestPayloadDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;
import org.hzero.boot.interfaces.sdk.invoke.InterfaceInvokeSdk;
import org.hzero.core.base.BaseAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ExternalServiceImpl extends BaseAppService implements ExternalService {
    @Autowired
    private InterfaceInvokeSdk interfaceInvokeSdk;

    public ResponsePayloadDTO invokeInterface(ExternalDTO externalDTO) {
        String nameSpace = externalDTO.getNameSpace();
        String serverCode = externalDTO.getServerCode();
        String interfaceCode = externalDTO.getInterfaceCode();
        String headerId = externalDTO.getHeaderId();

        RequestPayloadDTO requestPayloadDTO = new RequestPayloadDTO();
        Map<String,String> mapPathVariable = new HashMap<>();
        mapPathVariable.put("organizationId", "0");
        mapPathVariable.put("headerId", headerId);
        requestPayloadDTO.setPathVariableMap(mapPathVariable);

        Map<String,String> mapParam = new HashMap<>();
        mapParam.put("Authorization", externalDTO.getAuthorization());
        requestPayloadDTO.setHeaderParamMap(mapParam);

        return interfaceInvokeSdk.invoke(nameSpace, serverCode, interfaceCode, requestPayloadDTO);
    }
}
