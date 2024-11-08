package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.UserTaskDto;
import com.hand.demo.api.dto.WorkFlowEventRequestDto;
import com.hand.demo.api.dto.WorkflowDto;
import com.hand.demo.infra.constant.WorkflowException;
import com.sun.corba.se.spi.orbutil.threadpool.Work;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.workflow.WorkflowClient;
import org.hzero.boot.workflow.dto.RunInstance;
import org.hzero.boot.workflow.dto.RunTaskHistory;
import org.hzero.common.HZeroService;
import org.hzero.core.util.Results;
import org.hzero.mybatis.domian.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvCountHeaderService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvCountHeader;
import com.hand.demo.domain.repository.InvCountHeaderRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Inventory Count Header Table(InvCountHeader)应用服务
 *
 * @author
 * @since 2024-10-25 13:59:39
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
    public InvCountHeader approvalCallback(Long organizationId, WorkFlowEventRequestDto workFlowEventRequestDto) {

        String businessKey = workFlowEventRequestDto.getBusinessKey();

        Condition condition = new Condition(InvCountHeader.class);
        condition.createCriteria().andEqualTo("countNumber", businessKey);

        List<InvCountHeader> invCountHeaders = invCountHeaderRepository.selectByCondition(condition);

        if (invCountHeaders.isEmpty()) {
            throw new CommonException("Doc with businessKey " + businessKey + " not found");
        }

        InvCountHeader invCountHeader = invCountHeaders.get(0);

        invCountHeader.setCountStatus(workFlowEventRequestDto.getDocStatus());
        invCountHeader.setApprovedTime(workFlowEventRequestDto.getApprovedTime());
        invCountHeader.setWorkflowId(workFlowEventRequestDto.getWorkflowId());

        invCountHeaderRepository.updateByPrimaryKeySelective(invCountHeader);

        return invCountHeader;
    }

    @Override
    public RunInstance startInstance(Long organizationId, WorkflowDto workflowDto) {
        String businessKey = workflowDto.getBusinessKey();

        if (businessKey == null || businessKey.isEmpty()) {
            throw new CommonException(WorkflowException.BUSINESS_KEY_EXISTS, workflowDto.getBusinessKey());
        }

        Condition condition = new Condition(InvCountHeader.class);
        condition.createCriteria().andEqualTo("countNumber", businessKey);

        if (invCountHeaderRepository.selectByCondition(condition).isEmpty()) {
            throw new CommonException(WorkflowException.BUSINESS_KEY_EXISTS, workflowDto.getBusinessKey());
        }

        return workflowClient.startInstanceByFlowKey(
                organizationId,
                workflowDto.getFlowKey(),
                businessKey,
                workflowDto.getDimension(),
                workflowDto.getStarter(),
                workflowDto.getVariableMap()
        );
    }


    public ResponseEntity<?> withdrawInstance(Long organizationId, WorkflowDto workflowDto) {
        return Results.success(workflowClient.flowWithdrawFlowKey(organizationId, workflowDto.getFlowKey() ,workflowDto.getBusinessKey()));
    }

    public List<RunTaskHistory> approveHistory(Long organizationId, Long instanceId) {
        return workflowClient.approveHistory(organizationId, instanceId);
    }

}

