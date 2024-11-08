package com.hand.demo.app.service;

import com.hand.demo.api.controller.dto.InvoiceApplyLineDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.InvoiceApplyLine;

import java.util.List;

/**
 * (InvoiceApplyLine)应用服务
 *
 * @author Allan
 * @since 2024-11-04 11:21:14
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

    /**
     * 保存数据
     *
     * @param invoiceApplyLines 数据
     */
    void saveData(List<InvoiceApplyLine> invoiceApplyLines);

    /**
     * delete apply line object from db
     * @param invoiceApplyLines invoice line object
     */
    void deleteApplyLine(List<InvoiceApplyLine> invoiceApplyLines);

    /**
     * export all invoice lines with the corresponding to apply header number
     * @param dto dto object for filtering query
     * @return list of invoice line dto
     */
    List<InvoiceApplyLineDTO> exportAll(InvoiceApplyLineDTO dto);

}

