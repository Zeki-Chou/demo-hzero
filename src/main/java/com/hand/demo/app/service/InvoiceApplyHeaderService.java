package com.hand.demo.app.service;

import com.hand.demo.api.controller.dto.InvoiceApplyHeaderDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author
 * @since 2024-11-04 14:40:36
 */
public interface InvoiceApplyHeaderService {

    /**
     * 查询数据
     *
     * @param pageRequest        分页参数
     * @param invoiceApplyHeader 查询条件
     * @param organizationId     tenant id
     * @return 返回值
     */
    Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader, Long organizationId);

    /**
     * 保存数据
     *
     * @param invoiceApplyHeaders 数据
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
     * @return dto
     */
    InvoiceApplyHeaderDTO detail(Long applyHeaderId);

    /**
     * get a list of all dtos in db to be exported for excel
     * @param organizationId tenant id
     * @return list of header dtos
     */
    List<InvoiceApplyHeaderDTO> exportAll(Long organizationId);
}

