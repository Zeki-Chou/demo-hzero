package com.hand.demo.app.service;

import com.hand.demo.api.dto.ExternalDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;

public interface ExternalService {
    public ResponsePayloadDTO invokeInterface(ExternalDTO externalDTO);
}
