package com.hand.demo.app.service;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author
 * @since 2024-11-04 10:16:07
 */
public interface InvoiceApplyHeaderService {

    /**
     * 查询数据
     *
     * @param pageRequest         分页参数
//     * @param invoiceApplyHeaders 查询条件
     * @return 返回值
     */
    public Page<InvoiceApplyHeader> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader);

    /**
     * 保存数据
     *
     * @param invoiceApplyHeaders 数据
     */
    void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders);
    public void deleteData(Long headerId);
    public InvoiceApplyHeaderDTO detail(Long headerId);
    public List<InvoiceApplyHeaderDTO> exportAll (PageRequest pageRequest);
    public void countApplyLineUpdateHeader (Long header_id);
    public void countApplyLineUpdateWithHeader (InvoiceApplyHeader invoiceApplyHeader);
}