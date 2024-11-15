package com.hand.demo.infra.repository.impl;

import com.hand.demo.domain.dto.InvoiceApplyHeaderDTO;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.mapper.InvoiceApplyHeaderMapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * (InvoiceApplyHeader)资源库
 *
 * @author azhar.naufal@hand-global.com
 * @since 2024-11-04 10:11:55
 */
@Component
public class InvoiceApplyHeaderRepositoryImpl extends BaseRepositoryImpl<InvoiceApplyHeader> implements InvoiceApplyHeaderRepository {
    @Resource
    private InvoiceApplyHeaderMapper invoiceApplyHeaderMapper;

    @Override
    public List<InvoiceApplyHeaderDTO> selectList(InvoiceApplyHeaderDTO invoiceApplyHeader) {
        return invoiceApplyHeaderMapper.selectList(invoiceApplyHeader);
    }

    @Override
    public List<InvoiceApplyHeaderDTO> selectListDataSet(InvoiceApplyHeaderDTO invoiceApplyHeader) {
        return invoiceApplyHeaderMapper.selectListDataSet(invoiceApplyHeader);
    }

    @Override
    public InvoiceApplyHeader selectByPrimary(Long applyHeaderId) {
        InvoiceApplyHeader invoiceApplyHeader = new InvoiceApplyHeader();
        invoiceApplyHeader.setApplyHeaderId(applyHeaderId);
        InvoiceApplyHeader invoiceApplyHeaders = invoiceApplyHeaderMapper.selectOne(invoiceApplyHeader);

        return invoiceApplyHeaders;
    }

}

