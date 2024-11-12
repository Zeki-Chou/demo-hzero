package com.hand.demo.infra.mapper;

import com.hand.demo.api.controller.dto.InvoiceApplyHeaderDTO;
import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author Allan
 * @since 2024-11-04 14:40:36
 */
public interface InvoiceApplyHeaderMapper extends BaseMapper<InvoiceApplyHeader> {
    /**
     * 基础查询
     *
     * @param invoiceApplyHeader 查询条件
     * @return 返回值
     */
    List<InvoiceApplyHeaderDTO> selectList(InvoiceApplyHeader invoiceApplyHeader);

    /**
     * change del_flag value to 1
     *
     * @param invoiceApplyHeader apply header object
     */
    void updateDelFlag(InvoiceApplyHeader invoiceApplyHeader);

    /**
     * update invoice apply header info by apply header number
     * @param invoiceApplyHeader apply header object
     */
    void updateByHeaderNumber(InvoiceApplyHeader invoiceApplyHeader);
}

