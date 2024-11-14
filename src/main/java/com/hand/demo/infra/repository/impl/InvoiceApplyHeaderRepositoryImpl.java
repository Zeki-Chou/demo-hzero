package com.hand.demo.infra.repository.impl;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import org.apache.commons.collections.CollectionUtils;
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
 * @author
 * @since 2024-11-04 11:43:30
 */
@Component
public class InvoiceApplyHeaderRepositoryImpl extends BaseRepositoryImpl<InvoiceApplyHeader> implements InvoiceApplyHeaderRepository {
    @Resource
    private InvoiceApplyHeaderMapper invoiceApplyHeaderMapper;

    @Override
    public List<InvoiceApplyHeader> selectList(InvoiceApplyHeaderDTO invoiceApplyHeader) {
        return invoiceApplyHeaderMapper.selectList(invoiceApplyHeader);
    }

    @Override
    public InvoiceApplyHeader selectByPrimary(Long applyHeaderId) {
        InvoiceApplyHeaderDTO invoiceApplyHeader = new InvoiceApplyHeaderDTO();
        invoiceApplyHeader.setApplyHeaderId(applyHeaderId);
        List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceApplyHeaderMapper.selectList(invoiceApplyHeader);
        if (invoiceApplyHeaders.size() == 0) {
            return null;
        }
        return invoiceApplyHeaders.get(0);
    }

    @Override
    public void softDeleteById(Long applyHeaderId) {
        invoiceApplyHeaderMapper.softDeleteById(applyHeaderId);
    }

}

