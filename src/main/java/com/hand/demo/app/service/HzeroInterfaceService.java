package com.hand.demo.app.service;

import com.hand.demo.domain.dto.ExternalInterfaceDTO;
import org.hzero.boot.interfaces.sdk.dto.InterfaceDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;

import java.util.Map;

public interface HzeroInterfaceService {
    ResponsePayloadDTO invokeInterface(ExternalInterfaceDTO externalInterfaceDTO, String param);

    ResponsePayloadDTO invokeCalculator(String jsonString, ExternalInterfaceDTO externalInterfaceDTO);
}
