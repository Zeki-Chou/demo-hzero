package com.hand.demo.domain.repository;

import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.InvoiceApplyLine;

import java.util.List;

/**
 * (InvoiceApplyLine)资源库
 *
 * @author Allan
 * @since 2024-11-04 11:21:14
 */
public interface InvoiceApplyLineRepository extends BaseRepository<InvoiceApplyLine> {
    /**
     * 查询
     *
     * @param invoiceApplyLine 查询条件
     * @return 返回值
     */
    List<InvoiceApplyLine> selectList(InvoiceApplyLine invoiceApplyLine);

    /**
     * 根据主键查询（可关联表）
     *
     * @param applyLineId 主键
     * @return 返回值
     */
    InvoiceApplyLine selectByPrimary(Long applyLineId);

    List<InvoiceApplyLine> selectByHeaderIds(List<Long> invoiceApplyHeaderIds);
}
