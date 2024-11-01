package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.InvCountHeaderDTO;
import com.hand.demo.api.dto.WorkFlowEventRequestDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.interfaces.sdk.invoke.InterfaceInvokeSdk;
import org.hzero.boot.workflow.WorkflowClient;
import org.hzero.boot.workflow.dto.RunInstance;
import org.hzero.boot.workflow.dto.RunTaskHistory;
import org.hzero.mybatis.domian.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvCountHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvCountHeader;
import com.hand.demo.domain.repository.InvCountHeaderRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Inventory Count Header Table(InvCountHeader)应用服务
 *
 * @author
 * @since 2024-10-25 14:00:00
 */
@Service
public class InvCountHeaderServiceImpl implements InvCountHeaderService {
    private static final Logger logger = LoggerFactory.getLogger(InvCountHeaderDTO.class);

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
    public InvCountHeaderDTO approvalCallback(Long organizationId, WorkFlowEventRequestDTO workFlowEventRequestDTO) {
        Condition condition = new Condition(InvCountHeader.class);

        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo("countNumber", workFlowEventRequestDTO.getBusinessKey());
        List<InvCountHeader> invCountHeaderList =  invCountHeaderRepository.selectByCondition(condition);

//        InvCountHeader invCountHeader =  invCountHeaderRepository.selectOne(new InvCountHeader().setCountNumber(workFlowEventRequestDTO.getBusinessKey()));


        invCountHeaderList.get(0).setApprovedTime(workFlowEventRequestDTO.getApprovedTime());
        invCountHeaderList.get(0).setCountStatus(workFlowEventRequestDTO.getDocStatus());
        invCountHeaderList.get(0).setWorkflowId(workFlowEventRequestDTO.getWorkflowId());
        invCountHeaderRepository.updateByPrimaryKey(invCountHeaderList.get(0));

        InvCountHeaderDTO invCountHeaderDTO = new InvCountHeaderDTO();
        BeanUtils.copyProperties(invCountHeaderList.get(0), invCountHeaderDTO);
//        BeanUtils.copyProperties(invCountHeader, invCountHeaderDTO);

        logger.info(String.valueOf(invCountHeaderDTO));

        return  invCountHeaderDTO;
    }

    @Override
    public List<RunTaskHistory> history(Long organizationId, Long workflowId) {
        return workflowClient.approveHistory(organizationId, workflowId);
    }

    @Override
    public RunInstance startInstanceByFlowKey(Long organizationId, WorkFlowEventRequestDTO workFlowEventRequestDTO) {
        InvCountHeader invCountHeader = new InvCountHeader();
        invCountHeader.setCountNumber(workFlowEventRequestDTO.getBusinessKey());
        if(invCountHeaderRepository.selectOne(invCountHeader) == null) {
            throw new CommonException("demo47358.start_workflow_error", workFlowEventRequestDTO.getBusinessKey());
        }

        return workflowClient.startInstanceByFlowKey(organizationId, workFlowEventRequestDTO.getWorkflowKey(),
                workFlowEventRequestDTO.getBusinessKey(), workFlowEventRequestDTO.getDimension(),
                workFlowEventRequestDTO.getStarter(), workFlowEventRequestDTO.getVariabelMap());
    }

    @Override
    public Integer flowWithdrawFlowKey(Long organizationId, WorkFlowEventRequestDTO workFlowEventRequestDTO) {
        try {
            workflowClient.flowWithdrawFlowKey(organizationId, workFlowEventRequestDTO.getWorkflowKey(),
                    workFlowEventRequestDTO.getBusinessKey());
            return 1; // Success flag
        } catch (Exception e) {
            // Log or handle the exception as needed
            System.err.println("Error in flowWithdrawFlowKey: " + e.getMessage());
            return -1; // Failure flag
        }
    }

}

