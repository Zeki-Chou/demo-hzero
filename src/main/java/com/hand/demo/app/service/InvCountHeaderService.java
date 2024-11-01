package com.hand.demo.app.service;

import com.hand.demo.domain.dto.CountHeaderDTO;
import com.hand.demo.domain.dto.WorkflowDTO;
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
 * @since 2024-10-25 13:58:45
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

    InvCountHeader approvalCallback(Long organizationId, CountHeaderDTO paramCountHeaderDTO);

    RunInstance startInstance (Long organizationId, WorkflowDTO workflowDTO);

    List<RunTaskHistory> approveHistory(Long organizationId, Long instanceId);

    ResponseEntity<?> withdrawByFlowKey(Long organizationId, WorkflowDTO workflowDTO);
}

