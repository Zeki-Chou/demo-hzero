package com.hand.demo.domain.repository;

import com.hand.demo.domain.dto.InvoiceApplyHeaderDTO;
import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * (InvoiceApplyHeader)资源库
 *
 * @author azhar.naufal@hand-global.com
 * @since 2024-11-04 10:11:55
 */
public interface InvoiceApplyHeaderRepository extends BaseRepository<InvoiceApplyHeader> {
    /**
     * 查询
     *
     * @param invoiceApplyHeader 查询条件
     * @return 返回值
     */
    List<InvoiceApplyHeaderDTO> selectList(InvoiceApplyHeaderDTO invoiceApplyHeader);

    List<InvoiceApplyHeaderDTO> selectListDataSet(InvoiceApplyHeaderDTO invoiceApplyHeader);

    /**
     * 根据主键查询（可关联表）
     *
     * @param applyHeaderId 主键
     * @return 返回值
     */
    InvoiceApplyHeader selectByPrimary(Long applyHeaderId);
}
