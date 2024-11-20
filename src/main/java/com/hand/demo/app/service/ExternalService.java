package com.hand.demo.app.service;

import com.hand.demo.api.controller.dto.CalculatorDTO;
import com.hand.demo.api.controller.dto.HeaderExternalDTO;
import com.hand.demo.api.controller.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.controller.dto.TranslationDTO;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;

public interface ExternalService {
    /**
     * implement
     * @param dto
     * @return
     */
    ResponsePayloadDTO translateTextExternal(TranslationDTO dto);

    /**
     * @param dto
     * @return
     */
    ResponsePayloadDTO add(CalculatorDTO dto);

    /**
     * get invoice header detail by using apply header id from the input dto object
     * @param organizationId tenant id
     * @param dto header external dto
     * @return response payload dto
     */
    ResponsePayloadDTO getInvoiceHeaderDetailExternal(Long organizationId, HeaderExternalDTO dto);
}
