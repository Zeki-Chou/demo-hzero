package com.hand.demo.app.service;

import com.hand.demo.domain.dto.InvoiceApplyHeaderDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author azhar.naufal@hand-global.com
 * @since 2024-11-04 10:11:55
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

    InvoiceApplyHeader selectById(Long headerId);

    InvoiceApplyHeaderDTO detail(Long headerId);

    InvoiceApplyHeader getHeaderById(Long headerId);

    /**
     * 保存数据
     *
     * @param invoiceApplyHeaders 数据
     */
    void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders);

    void softDelete(Long applyHeaderId);

    void updateByPrimaryKeySelective(InvoiceApplyHeader header);
}

