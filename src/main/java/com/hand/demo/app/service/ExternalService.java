package com.hand.demo.app.service;

import com.hand.demo.api.dto.ExternalRequestDTO;
import org.hzero.boot.interfaces.sdk.dto.InterfaceDTO;
import org.hzero.boot.interfaces.sdk.dto.RequestPayloadDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;

public interface ExternalService {
    ResponsePayloadDTO invokeInterface(String jsonString, ExternalRequestDTO externalRequestDTO);
    ResponsePayloadDTO invokeXML(String requestXML, ExternalRequestDTO externalRequestDTO);
    ResponsePayloadDTO invokeGetListAzhar(String jsonString, ExternalRequestDTO externalRequestDTO);
}
