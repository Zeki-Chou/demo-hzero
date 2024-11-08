package com.hand.demo.app.service;

import com.hand.demo.domain.entity.User;
import org.hzero.boot.interfaces.sdk.dto.InterfaceDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;

public interface InterfaceService {
    ResponsePayloadDTO translate(String text, String namespace, String serverCode, String interfaceCode);

    ResponsePayloadDTO calculate(String jsonString, String namespace, String serverCode, String interfaceCode);
}
