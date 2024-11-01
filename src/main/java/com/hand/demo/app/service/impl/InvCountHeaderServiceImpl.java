package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.*;
import com.hand.demo.infra.constant.Constants;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.workflow.WorkflowClient;
import org.hzero.boot.workflow.dto.RunInstance;
import org.hzero.boot.workflow.dto.RunTaskHistory;
import org.hzero.mybatis.domian.Condition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvCountHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvCountHeader;
import com.hand.demo.domain.repository.InvCountHeaderRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Inventory Count Header Table(InvCountHeader)应用服务
 *
 * @author
 * @since 2024-10-25 14:05:12
 */
@Service
public class InvCountHeaderServiceImpl implements InvCountHeaderService {
    private final InvCountHeaderRepository invCountHeaderRepository;
    private final WorkflowClient workflowClient;


    @Autowired
    public  InvCountHeaderServiceImpl(InvCountHeaderRepository invCountHeaderRepository, WorkflowClient workflowClient){
        this.invCountHeaderRepository=invCountHeaderRepository;
        this.workflowClient=workflowClient;
    }
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
    public InvCountHeaderDTO approvalCallback(Long tenantId, WorkflowEventDTO workflowEventDTO) {
        Condition condition = new Condition(InvCountHeader.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo("countNumber", workflowEventDTO.getBusinessKey());
        List<InvCountHeader> invCountHeaderList =  invCountHeaderRepository.selectByCondition(condition);
        if(invCountHeaderList.isEmpty()){
            throw  new CommonException("Entity not found");
        }

        InvCountHeader invCountHeader = invCountHeaderList.get(0);
        invCountHeader.setApprovedTime(workflowEventDTO.getApprovedTime());
        invCountHeader.setCountStatus(workflowEventDTO.getDocStatus());
        invCountHeader.setWorkflowId(workflowEventDTO.getWorkflowId());
        invCountHeaderRepository.updateByPrimaryKey(invCountHeader);
        invCountHeaderRepository.updateOptional(invCountHeader,
                InvCountHeader.FIELD_WORKFLOW_ID,
                InvCountHeader.FIELD_COUNT_STATUS,
                InvCountHeader.FIELD_APPROVED_TIME);

        InvCountHeaderDTO invCountHeaderDTO = new InvCountHeaderDTO();
        BeanUtils.copyProperties(invCountHeader, invCountHeaderDTO);
        return  invCountHeaderDTO;
    }

    @Override
    public Long startApprovalWorkflow(Long tenantId, WorkflowStartDTO workflowStartDTO){
        RunInstance runInstance = null;

        Condition condition = new Condition(InvCountHeader.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo("countNumber", workflowStartDTO.getBusinessKey());
        List<InvCountHeader> invCountHeaderList =  invCountHeaderRepository.selectByCondition(condition);
        if(invCountHeaderList.isEmpty()){
            throw  new CommonException(Constants.WORKFLOW_ERROR_MULTILINGUAL,workflowStartDTO.getBusinessKey());
        }

        runInstance = workflowClient.startInstanceByFlowKey(tenantId, workflowStartDTO.getFlowKey(),
                workflowStartDTO.getBusinessKey(), workflowStartDTO.getDimension(),
                workflowStartDTO.getStarter(), workflowStartDTO.getVariableMap());


        return runInstance.getInstanceId();
    }

    @Override
    public void withdrawApprovalWorkflow(Long tenantId, WorkflowWithdrawDTO workflowWithdrawDTO){
        try {
            workflowClient.withdraw(tenantId, workflowWithdrawDTO.getInstanceIds(),workflowWithdrawDTO.getCheckFlag());
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public List<RunTaskHistory> getApprovalWorkflowHistory(Long tenantId, WorkflowApproveHistoryDTO workflowApproveHistoryDTO){
        List<RunTaskHistory> runTaskHistory = null;
        try{
            runTaskHistory = workflowClient.approveHistory(tenantId,workflowApproveHistoryDTO.getInstanceId());
            if(runTaskHistory.isEmpty()){
                throw new CommonException("Get Approval History");
            }
        } catch (Exception e){
            throw new CommonException(e.getMessage());
        }
        return runTaskHistory;
    }
}

