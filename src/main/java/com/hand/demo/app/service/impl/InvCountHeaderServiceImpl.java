package com.hand.demo.app.service.impl;

import com.hand.demo.domain.dto.CountHeaderDTO;
import com.hand.demo.domain.dto.WorkflowDTO;
import com.hand.demo.infra.constant.WorkFlowExeption;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.workflow.WorkflowClient;
import org.hzero.boot.workflow.dto.RunInstance;
import org.hzero.boot.workflow.dto.RunTaskHistory;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvCountHeaderService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvCountHeader;
import com.hand.demo.domain.repository.InvCountHeaderRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Inventory Count Header Table(InvCountHeader)应用服务
 *
 * @author
 * @since 2024-10-25 13:58:45
 */
@Service
public class InvCountHeaderServiceImpl implements InvCountHeaderService {
    @Autowired
    private InvCountHeaderRepository invCountHeaderRepository;
    @Autowired
    private WorkflowClient workflowClient;

    @Override
    public Page<InvCountHeader> selectList(PageRequest pageRequest, InvCountHeader invCountHeader) {
        return PageHelper.doPageAndSort(pageRequest, () -> invCountHeaderRepository.selectList(invCountHeader));
    }

    @Override
    public void saveData(List<InvCountHeader> invCountHeaders) {
        List<InvCountHeader> insertList = invCountHeaders.stream().filter(line -> line.getCountHeaderId() == null).collect(Collectors.toList());
        List<InvCountHeader> updateList = invCountHeaders.stream().filter(line -> line.getCountHeaderId() != null).collect(Collectors.toList());
        invCountHeaderRepository.batchInsertSelective(insertList);
        invCountHeaderRepository.batchUpdateByPrimaryKeySelective(updateList);
    }

    @Override
    public InvCountHeader approvalCallback(Long organizationId, CountHeaderDTO paramCountHeaderDTO){
        InvCountHeader exInvCountHeader = new InvCountHeader();
        exInvCountHeader.setCountNumber(paramCountHeaderDTO.getBusinessKey());

        InvCountHeader invCountHeader = invCountHeaderRepository.selectOne(exInvCountHeader);
        invCountHeader.setWorkflowId(paramCountHeaderDTO.getWorkflowId());
        invCountHeader.setCountStatus(paramCountHeaderDTO.getDocStatus());
        invCountHeader.setApprovedTime(paramCountHeaderDTO.getApprovedTime());

        invCountHeaderRepository.updateByPrimaryKey(invCountHeader);

        return invCountHeader;

    }

    @Override
    public RunInstance startInstance(Long organizationId, WorkflowDTO workflowDTO){
        InvCountHeader exInvCountHeader = new InvCountHeader();
        exInvCountHeader.setCountNumber(workflowDTO.getBusinessKey());
        InvCountHeader invCountHeader = invCountHeaderRepository.selectOne(exInvCountHeader);

        if(invCountHeader == null){
            throw new CommonException(WorkFlowExeption.MESSAGE_ENCODING, workflowDTO.getBusinessKey());
        }else {
            String flowKey = workflowDTO.getFlowKey();
            String businessKey = workflowDTO.getBusinessKey();
            String dimension = workflowDTO.getDimension();
            String starter = workflowDTO.getStarter();
            Map<String, Object> variableMap = workflowDTO.getVariableMap();

            RunInstance runInstance = workflowClient.startInstanceByFlowKey(organizationId, flowKey, businessKey, dimension, starter, variableMap);
            return runInstance;
        }

    }

    @Override
    public List<RunTaskHistory> approveHistory(Long organizationId, Long instanceId){
        return workflowClient.approveHistory(organizationId, instanceId);
    }

    @Override
    public ResponseEntity<?> withdrawByFlowKey(Long organizationId, WorkflowDTO workflowDTO){
        String flowKey = workflowDTO.getFlowKey();
        String businessKey = workflowDTO.getBusinessKey();

        return Results.success(workflowClient.flowWithdrawFlowKey(organizationId, flowKey, businessKey));
    }
}

