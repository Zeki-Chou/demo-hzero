package com.hand.demo.app.service;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.dto.InvoiceApplyHeaderReportDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author
 * @since 2024-11-04 13:16:06
 */
public interface InvoiceApplyHeaderService {

    /**
     * 查询数据
     *
     * @param pageRequest         分页参数
     * @param invoiceApplyHeaderDTO 查询条件
     * @return 返回值
     */
    Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeaderDTO invoiceApplyHeaderDTO);

    InvoiceApplyHeaderDTO detail(Long id);

    /**
     * 保存数据
     *
     * @param invoiceApplyHeaders 数据
     */
    void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders);


    void delete(List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOS);

    InvoiceApplyHeaderReportDTO report(Long organizationId,InvoiceApplyHeaderReportDTO invoiceApplyHeaderReportDTO);
}

