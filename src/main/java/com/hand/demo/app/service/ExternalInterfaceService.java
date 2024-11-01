package com.hand.demo.app.service;

import com.hand.demo.api.dto.ExternalInterfaceDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;

public interface ExternalInterfaceService {

    ResponsePayloadDTO invokeInterface(Long organizationId, ExternalInterfaceDTO externalInterfaceDTO);
}
