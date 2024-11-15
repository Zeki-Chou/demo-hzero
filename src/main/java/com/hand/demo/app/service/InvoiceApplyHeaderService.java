package com.hand.demo.app.service;

import com.hand.demo.domain.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.domain.dto.InvoiceApplyReportQueryDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.core.base.BaseConstants;

import java.util.List;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author azhar.naufal@hand-global.com
 * @since 2024-11-04 10:11:55
 */
public interface InvoiceApplyHeaderService {

    /**
     * 查询数据
     *
     * @param pageRequest         分页参数
     * @param invoiceApplyHeadersDTO 查询条件
     * @return 返回值
     */
    Page<InvoiceApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeaderDTO invoiceApplyHeadersDTO);

    /**
     * Select List for Export
     *
     * @param pageRequest         分页参数
     * @param invoiceApplyHeaderDTO 查询条件
     * @return 返回值
     */
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    Page<InvoiceApplyHeaderDTO> selectListExport(PageRequest pageRequest, InvoiceApplyHeaderDTO invoiceApplyHeaderDTO);

    /**
     * Select List for Export
     *
     * @param headerId id
     * @return 返回值
     */
    InvoiceApplyHeaderDTO detail(Long headerId);

    InvoiceApplyHeader getHeaderById(Long headerId);

    /**
     * 保存数据
     *
     * @param invoiceApplyHeaders 数据
     */
    void saveData(List<InvoiceApplyHeaderDTO> invoiceApplyHeaders);

    void softDelete(Long applyHeaderId);

    void batchSoftDelete(List<InvoiceApplyHeaderDTO> applyHeaderDTOS);

    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    List<InvoiceApplyHeaderDTO> selectListForDataSet(InvoiceApplyHeaderDTO invoiceApplyHeaderDTO);

    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    List<InvoiceApplyReportQueryDTO> selectListForExcel(InvoiceApplyReportQueryDTO invoiceApplyReportQueryDTO, Long organizationId);
}

