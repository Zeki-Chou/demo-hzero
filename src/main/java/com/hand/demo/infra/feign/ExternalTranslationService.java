package com.hand.demo.infra.feign;

import com.hand.demo.api.dto.CalculatorRequest;
import com.hand.demo.api.dto.TranslationRequest;
import org.hzero.boot.interfaces.sdk.dto.RequestPayloadDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;

public interface ExternalTranslationService {
    ResponsePayloadDTO invokeInterface(TranslationRequest request);
    ResponsePayloadDTO invokeCalculatorInterface(CalculatorRequest request);
}
