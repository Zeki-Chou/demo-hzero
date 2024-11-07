package com.hand.demo.app.service;

import com.hand.demo.api.dto.InvoiceApplyHeaderDto;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author
 * @since 2024-11-04 11:01:41
 */
public interface InvoiceApplyHeaderService {

    /**
     * 查询数据
     *
     * @param pageRequest         分页参数
     * @param invoiceApplyHeaders 查询条件
     * @return 返回值
     */
    Page<InvoiceApplyHeaderDto> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeaders);

    Page<InvoiceApplyHeader> exportList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader);

    /**
     * 保存数据
     *
     * @param invoiceApplyHeaders 数据
     */
    List<InvoiceApplyHeaderDto> saveData(List<InvoiceApplyHeaderDto> invoiceApplyHeaders);

    void deleteInvoiceHeaders(List<InvoiceApplyHeader> invoiceApplyHeaders);

    InvoiceApplyHeaderDto getInvoiceDetailById(Long applyHeaderId);
}

