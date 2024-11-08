package com.hand.demo.app.service;

import com.hand.demo.api.dto.WorkFlowEventRequestDto;
import com.hand.demo.api.dto.WorkflowDto;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.InvCountHeader;
import org.hzero.boot.workflow.dto.RunInstance;
import org.hzero.boot.workflow.dto.RunTaskHistory;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Inventory Count Header Table(InvCountHeader)应用服务
 *
 * @author
 * @since 2024-10-25 13:59:39
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

    InvCountHeader approvalCallback(Long organizationId, WorkFlowEventRequestDto workFlowEventRequestDto);

    RunInstance startInstance(Long organizationId, WorkflowDto workflowDto);

    ResponseEntity<?> withdrawInstance (Long organizationId, WorkflowDto workflowDto);

    List<RunTaskHistory> approveHistory(Long organizationId, Long instanceId);
}

