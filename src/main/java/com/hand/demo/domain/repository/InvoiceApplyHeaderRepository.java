package com.hand.demo.domain.repository;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.dto.InvoiceApplyInfoDTO;
import com.hand.demo.api.dto.InvoiceApplyInfoDTOOut;
import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * (InvoiceApplyHeader)资源库
 *
 * @author
 * @since 2024-11-04 10:16:07
 */
public interface InvoiceApplyHeaderRepository extends BaseRepository<InvoiceApplyHeader> {
    /**
     * 查询
     *
     * @param invoiceApplyHeader 查询条件
     * @return 返回值
     */
    List<InvoiceApplyHeader> selectList(InvoiceApplyHeader invoiceApplyHeader);

    /**
     * 根据主键查询（可关联表）
     *
     * @param applyHeaderId 主键
     * @return 返回值
     */
    InvoiceApplyHeader selectByPrimary(Long applyHeaderId);

    public List<InvoiceApplyInfoDTOOut> invoiceApplyInfo(InvoiceApplyInfoDTO invoiceApplyInfoDTO);
}