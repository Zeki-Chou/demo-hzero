package com.hand.demo.app.service;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.dto.InvoiceApplyInfoDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author
 * @since 2024-11-04 11:43:30
 */
public interface InvoiceApplyHeaderService {

    /**
     * 查询数据
     *
     * @param pageRequest         分页参数
     * @param invoiceApplyHeaders 查询条件
     * @return 返回值
     */
    Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeaders);

    /**
     * 保存数据
     *
     * @param invoiceApplyHeaderDTOs 数据
     */
    void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaderDTOs);

    void softDeleteById(Long applyHeaderId);

    InvoiceApplyHeaderDTO detail(Long applyHeaderId);

    InvoiceApplyInfoDTO getInvoiceApplyInfo(InvoiceApplyInfoDTO infoDTO);

}