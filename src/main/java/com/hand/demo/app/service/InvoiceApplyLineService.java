package com.hand.demo.app.service;

import com.hand.demo.domain.dto.InvoiceApplyLineDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.InvoiceApplyLine;

import java.util.List;

/**
 * (InvoiceApplyLine)应用服务
 *
 * @author azhar.naufal@hand-global.com
 * @since 2024-11-04 11:16:15
 */
public interface InvoiceApplyLineService {

    /**
     * 查询数据
     *
     * @param pageRequest       分页参数
     * @param invoiceApplyLines 查询条件
     * @return 返回值
     */
    Page<InvoiceApplyLine> selectList(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLines);

    Page<InvoiceApplyLineDTO> selectListExport(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine);

    List<InvoiceApplyLine> linesByHeaderId(Long headerId);

    /**
     * 保存数据
     *
     * @param invoiceApplyLines 数据
     */
    void saveData(List<InvoiceApplyLine> invoiceApplyLines);

    void deleteData(List<InvoiceApplyLine> invoiceApplyLines);
}

