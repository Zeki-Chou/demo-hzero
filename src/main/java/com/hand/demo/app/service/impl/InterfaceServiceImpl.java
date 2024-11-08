package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.app.service.InterfaceService;
import com.hand.demo.app.service.UserService;
import com.hand.demo.domain.entity.User;
import io.choerodon.core.exception.CommonException;
import org.hzero.boot.interfaces.sdk.dto.InterfaceDTO;
import org.hzero.boot.interfaces.sdk.dto.RequestPayloadDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;
import org.hzero.boot.interfaces.sdk.invoke.InterfaceInvokeSdk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class InterfaceServiceImpl implements InterfaceService {

    @Autowired
    private InterfaceInvokeSdk interfaceInvokeSdk;

    @Autowired
    private UserService userService;

    @Override
    public ResponsePayloadDTO translate(String param, String namespace, String serverCode, String interfaceCode) {

        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("text", param);

        RequestPayloadDTO requestPayloadDTO = new RequestPayloadDTO();
        requestPayloadDTO.setRequestParamMap(requestParams);

        InterfaceDTO interfaceDto = new InterfaceDTO();
        interfaceDto.setNamespace(namespace);
        interfaceDto.setServerCode(serverCode);
        interfaceDto.setInterfaceCode(interfaceCode);

        return interfaceInvokeSdk.invoke(
                interfaceDto.getNamespace(),
                interfaceDto.getServerCode(),
                interfaceDto.getInterfaceCode(),
                requestPayloadDTO
        );
    }

    @Override
    public ResponsePayloadDTO calculate(String xmlString, String namespace, String serverCode, String interfaceCode) {

        InterfaceDTO interfaceDto = new InterfaceDTO();
        interfaceDto.setNamespace(namespace);
        interfaceDto.setServerCode(serverCode);
        interfaceDto.setInterfaceCode(interfaceCode);

        RequestPayloadDTO requestPayloadDTO = new RequestPayloadDTO();
        requestPayloadDTO.setPayload(xmlString);
        requestPayloadDTO.setMediaType("application/soap+xml");

        return interfaceInvokeSdk.invoke(
                interfaceDto.getNamespace(),
                interfaceDto.getServerCode(),
                interfaceDto.getInterfaceCode(),
                requestPayloadDTO
        );
    }
}
