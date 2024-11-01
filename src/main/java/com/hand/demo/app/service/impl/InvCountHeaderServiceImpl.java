package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.InvCountHeaderDto;
import com.hand.demo.api.dto.StartWorkflowRequest;
import com.hand.demo.api.dto.WithdrawWorkflowRequest;
import com.hand.demo.domain.entity.Task;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.checkerframework.checker.units.qual.C;
import org.hzero.boot.workflow.WorkflowClient;
import org.hzero.boot.workflow.dto.RunInstance;
import org.hzero.boot.workflow.dto.RunTaskHistory;
import org.hzero.mybatis.domian.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvCountHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvCountHeader;
import com.hand.demo.domain.repository.InvCountHeaderRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Inventory Count Header Table(InvCountHeader)应用服务
 *
 * @author
 * @since 2024-10-25 13:59:58
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
    public InvCountHeader approvalCallback(Long organizationId, InvCountHeaderDto invCountHeaderDto) {
        Condition condition = new Condition(InvCountHeader.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo("countNumber", invCountHeaderDto.getBusinessKey());
        List<InvCountHeader> invCountHeaders = invCountHeaderRepository.selectByCondition(condition);
        if(!invCountHeaders.isEmpty() && invCountHeaders.get(0) != null) {
            InvCountHeader invCountHeader = invCountHeaders.get(0);
            invCountHeader.setCountStatus(invCountHeaderDto.getDocStatus());
            invCountHeader.setApprovedTime(convertToDate(invCountHeaderDto.getApprovedTime()));
            invCountHeader.setWorkflowId(invCountHeaderDto.getWorkflowId());
//            invCountHeader.setCountNumber(invCountHeaderDto.getBusinessKey());
            invCountHeaderRepository.updateByPrimaryKeySelective(invCountHeader);
            return invCountHeader;
        }
        return null;
    }

    @Override
    public List<RunTaskHistory> showApproveHistory(Long organizationId, Long instanceId) {
        return workflowClient.approveHistory(organizationId, instanceId);
    }

    @Override
    public RunInstance startWorkflow(Long organizationId, StartWorkflowRequest request) {
//        Condition condition = new Condition(InvCountHeader.class);
//        Condition.Criteria criteria = condition.createCriteria();
//        criteria.andEqualTo("countNumber", request.getBusinessKey());
        InvCountHeader countHeader = new InvCountHeader();
        countHeader.setCountNumber(request.getBusinessKey());
        InvCountHeader invCountHeader = invCountHeaderRepository.selectOne(countHeader);

        if(invCountHeader == null) {
            throw new CommonException("demo47355.start_workflow_error", request.getBusinessKey());
        }
        return workflowClient.startInstanceByFlowKey(
                organizationId,
                request.getFlowKey(),
                request.getBusinessKey(),
                request.getDimension(),
                request.getStarter(),
                request.getVariableMap()
        );
    }

    @Override
    public void withdrawWorkflow(Long organizationId, WithdrawWorkflowRequest request) {
        workflowClient.flowWithdrawFlowKey(
                organizationId,
                request.getFlowKey(),
                request.getBusinessKey()
        );
    }


    public Date convertToDate(String dateString) {
        System.out.println("cjwebcuwec " + dateString);
        if (dateString == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}

