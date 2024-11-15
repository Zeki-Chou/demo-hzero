package com.hand.demo.infra.mapper;

import com.hand.demo.domain.dto.InvoiceApplyHeaderDTO;
import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author azhar.naufal@hand-global.com
 * @since 2024-11-04 10:11:55
 */
public interface InvoiceApplyHeaderMapper extends BaseMapper<InvoiceApplyHeader> {
    /**
     * 基础查询
     * @param invoiceApplyHeader 查询条件
     * @return 返回值
     */
    List<InvoiceApplyHeaderDTO> selectList(InvoiceApplyHeaderDTO invoiceApplyHeader);

    /**
     * 基础查询
     * @param invoiceApplyHeader 查询条件
     * @return 返回值
     */
    List<InvoiceApplyHeaderDTO> selectListDataSet(InvoiceApplyHeaderDTO invoiceApplyHeader);
}



