package com.hand.demo.app.service;

import com.hand.demo.api.controller.dto.InvoiceApplyHeaderDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author Allan
 * @since 2024-11-04 14:40:36
 */
public interface InvoiceApplyHeaderService {

    /**
     * find list of invoice header object based on criteria of invoiceApplyHeader object
     *
     * @param pageRequest        page request
     * @param invoiceApplyHeader invoice header object criteria
     * @param organizationId     tenant id
     * @return page of invoice header dto object
     */
    Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader, Long organizationId);

    /**
     * save and update data apply header
     *
     * @param invoiceApplyHeaders list of invoice header dto
     */
    void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders, Long organizationId);

    /**
     * change del flag to 1
     * @param invoiceApplyHeader header object
     */
    void deleteData(InvoiceApplyHeader invoiceApplyHeader);

    /**
     * get apply header detail with their apply lines
     * @param applyHeaderId id
     * @return header dto object with list of invoice lines corresponding to that header
     */
    InvoiceApplyHeaderDTO detail(Long applyHeaderId);

    /**
     * get a list of all invoice header dto in db to be exported for Excel
     * @param organizationId tenant id
     * @return list of header dto
     */
    List<InvoiceApplyHeaderDTO> exportAll(Long organizationId);
}

