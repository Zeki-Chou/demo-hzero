package com.hand.demo.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.InvoiceApplyLine;

import java.util.List;

/**
 * (InvoiceApplyLine)应用服务
 *
 * @author Allan
 * @since 2024-11-04 11:21:13
 */
public interface InvoiceApplyLineMapper extends BaseMapper<InvoiceApplyLine> {
    /**
     * 基础查询
     *
     * @param invoiceApplyLine 查询条件
     * @return 返回值
     */
    List<InvoiceApplyLine> selectList(InvoiceApplyLine invoiceApplyLine);

    /**
     * get list of invoice line object where their header ids is in the list
     *
     * @param invoiceApplyHeaderIds list of header ids
     * @return list of lines with corresponding header ids
     */
    List<InvoiceApplyLine> selectByHeaderId(List<Long> invoiceApplyHeaderIds);
}

