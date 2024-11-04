package com.hand.demo.app.service;

import com.hand.demo.api.controller.dto.InvCountHeadersDTO;
import com.hand.demo.api.controller.dto.WorkFlowDTO;
import com.hand.demo.api.controller.dto.WorkFlowInstanceDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.InvCountHeader;
import org.hzero.boot.workflow.dto.RunTaskHistory;

import java.util.List;

/**
 * Inventory Count Header Table(InvCountHeader)应用服务
 *
 * @author
 * @since 2024-10-25 14:03:00
 */
public interface InvCountHeaderService {

    /**
     * 查询数据
     *
     * @param pageRequest     分页参数
     * @param invCountHeaders 查询条件
     * @return 返回值
     */
    Page<InvCountHeader> selectList(PageRequest pageRequest, InvCountHeader invCountHeaders);

    /**
     * 保存数据
     *
     * @param invCountHeaders 数据
     */
    void saveData(List<InvCountHeader> invCountHeaders);

    InvCountHeader updateApprovalCallback(WorkFlowDTO dto);

    InvCountHeader startWorkflow(Long organizationId, WorkFlowInstanceDTO dto);

    Void withdrawWorkflow(Long organizationId, WorkFlowInstanceDTO dto);

    List<RunTaskHistory> showApprovedHistory(Long organizationId, WorkFlowInstanceDTO dto);

}

