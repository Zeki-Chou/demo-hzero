package com.hand.demo.app.service;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.dto.InvoiceHeaderReportDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author Fatih Khoiri
 * @since 2024-11-04 10:14:16
 */
public interface InvoiceApplyHeaderService {

    /**
     * 查询数据
     *
     * @param pageRequest         分页参数
     * @param invoiceApplyHeaders 查询条件
     * @return 返回值
     */
    Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeaderDTO invoiceApplyHeaders);

    /**
     * 保存数据
     *
     * @param invoiceApplyHeaders 数据
     */
    void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders);

    InvoiceApplyHeaderDTO delete(Long id);

    InvoiceApplyHeaderDTO detail(Long id);

    InvoiceHeaderReportDTO detailReportExcel(InvoiceApplyHeaderDTO invoiceApplyHeaderDTO, Long organizationId);
}

