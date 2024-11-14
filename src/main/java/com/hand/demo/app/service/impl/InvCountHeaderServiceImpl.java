package com.hand.demo.app.service.impl;

import com.hand.demo.api.controller.dto.InvCountHeadersDTO;
import com.hand.demo.api.controller.dto.WorkFlowDTO;
import com.hand.demo.api.controller.dto.WorkFlowInstanceDTO;
import com.hand.demo.infra.constant.InvCountHeaderConstant;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.workflow.WorkflowClient;
import org.hzero.boot.workflow.dto.RunInstance;
import org.hzero.boot.workflow.dto.RunTaskHistory;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvCountHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvCountHeader;
import com.hand.demo.domain.repository.InvCountHeaderRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Inventory Count Header Table(InvCountHeader)应用服务
 *
 * @author Allan
 * @since 2024-10-25 14:03:01
 */
@Service
public class InvCountHeaderServiceImpl implements InvCountHeaderService {
    @Autowired
    private InvCountHeaderRepository invCountHeaderRepository;

    @Autowired
    private WorkflowClient client;

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
    public InvCountHeader updateApprovalCallback(WorkFlowDTO dto) {

        // assume that the data exist in database by manual database entry
        InvCountHeader record = new InvCountHeader();
        record.setCountNumber(dto.getBusinessKey());
        InvCountHeader header = invCountHeaderRepository.selectOne(record);

        header.setCountStatus(dto.getDocStatus());
        header.setWorkflowId(dto.getWorkflowId());
        header.setApprovedTime(dto.getApprovedTime());

        invCountHeaderRepository.updateByPrimaryKey(header);
        return header;
    }

    @Override
    public InvCountHeader startWorkflow(Long organizationId, WorkFlowInstanceDTO dto) {
        InvCountHeader header = new InvCountHeader();
        header.setCountNumber(dto.getBusinessKey());

        if (invCountHeaderRepository.selectOne(header) == null) {
            throw new CommonException("demo47359.start_workflow_error", dto.getBusinessKey());
        }

        Map<String, Object> variableMap = new HashMap<>();

        // this workflow requires amount
        variableMap.put("amount", 1000000);

        RunInstance startInstance = client.startInstanceByFlowKey(
                organizationId,
                dto.getFlowKey(),
                dto.getBusinessKey(),
                InvCountHeaderConstant.DIMENSION,
                InvCountHeaderConstant.STARTER,
                variableMap
        );

        InvCountHeadersDTO returnDTO = new InvCountHeadersDTO();

        returnDTO.setWorkflowId(startInstance.getInstanceId());
        returnDTO.setCreationDate(startInstance.getStartDate());
        returnDTO.setApprovedTime(startInstance.getEndDate());
        returnDTO.setCountNumber(startInstance.getBusinessKey());

        return returnDTO;
    }

    @Override
    public Void withdrawWorkflow(Long organizationId, WorkFlowInstanceDTO dto) {
        return client.flowWithdrawFlowKey(organizationId, dto.getFlowKey(), dto.getBusinessKey());
    }

    @Override
    public List<RunTaskHistory> showApprovedHistory(Long organizationId, WorkFlowInstanceDTO dto) {
        return client.approveHistoryByFlowKey(organizationId, dto.getFlowKey(), dto.getBusinessKey());
    }

}

