package com.hand.demo.app.service;

import com.hand.demo.api.controller.dto.CalculatorDTO;
import com.hand.demo.api.controller.dto.TranslationDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;

public interface ExternalService {
    ResponsePayloadDTO translateTextExternal(TranslationDTO dto);
    ResponsePayloadDTO add(CalculatorDTO dto);
}
