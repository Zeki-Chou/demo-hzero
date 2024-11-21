package com.hand.demo.domain.repository;

import com.hand.demo.api.controller.dto.InvoiceApplyHeaderDTO;
import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * (InvoiceApplyHeader)资源库
 *
 * @author Allan
 * @since 2024-11-04 14:40:36
 */
public interface InvoiceApplyHeaderRepository extends BaseRepository<InvoiceApplyHeader> {
    /**
     * 查询
     *
     * @param invoiceApplyHeader 查询条件
     * @return 返回值
     */
    List<InvoiceApplyHeaderDTO> selectList(InvoiceApplyHeaderDTO invoiceApplyHeader);

    /**
     * 根据主键查询（可关联表）
     *
     * @param applyHeaderId 主键
     * @return 返回值
     */
    InvoiceApplyHeaderDTO selectByPrimary(Long applyHeaderId);

    /**
     * update by invoice apply header number
     * @param invoiceApplyHeader invoice apply header entity
     */
    void updateByHeaderNumber(InvoiceApplyHeader invoiceApplyHeader);

    /**
     * delete by changing del_flag
     * @param invoiceApplyHeader invoice apply header entity
     */
    void deleteWithFlag(InvoiceApplyHeader invoiceApplyHeader);
}
