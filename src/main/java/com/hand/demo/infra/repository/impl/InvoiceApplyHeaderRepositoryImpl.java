package com.hand.demo.infra.repository.impl;

import com.hand.demo.api.dto.InvoiceApplyHeaderDTO;
import com.hand.demo.api.dto.InvoiceApplyHeaderReportDTO;
import com.hand.demo.infra.constant.Constants;
import org.apache.commons.collections.CollectionUtils;
import org.hzero.boot.apaas.common.userinfo.infra.feign.IamRemoteService;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.mapper.InvoiceApplyHeaderMapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * (InvoiceApplyHeader)资源库
 *
 * @author
 * @since 2024-11-04 16:02:27
 */
@Component
public class InvoiceApplyHeaderRepositoryImpl extends BaseRepositoryImpl<InvoiceApplyHeader> implements InvoiceApplyHeaderRepository {
    @Resource
    private InvoiceApplyHeaderMapper invoiceApplyHeaderMapper;

    @Autowired
    private IamRemoteService iamRemoteService;

    @Override
    public List<InvoiceApplyHeader> selectList(InvoiceApplyHeaderDTO invoiceApplyHeaderDTO) {
        setUserCondition(invoiceApplyHeaderDTO);
        return invoiceApplyHeaderMapper.selectList(invoiceApplyHeaderDTO);
    }

    @Override
    public List<InvoiceApplyHeader> report(InvoiceApplyHeaderReportDTO invoiceApplyHeaderReportDTO) {
        String userJson = iamRemoteService.selectSelf().getBody();
        JSONObject jsonObject= new JSONObject(userJson);
        invoiceApplyHeaderReportDTO.setTenantName(jsonObject.getString(Constants.REMOTE_SERVICE_TENANT_NAME));
        invoiceApplyHeaderReportDTO.setUserName(jsonObject.getString(Constants.REMOTE_SERVICE_REAL_NAME));
        return invoiceApplyHeaderMapper.report(invoiceApplyHeaderReportDTO);
    }

    @Override
    public InvoiceApplyHeader selectByPrimary(Long applyHeaderId) {
        InvoiceApplyHeaderDTO invoiceApplyHeaderDTO = new InvoiceApplyHeaderDTO();
        setUserCondition(invoiceApplyHeaderDTO);
        invoiceApplyHeaderDTO.setApplyHeaderId(applyHeaderId);
        List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceApplyHeaderMapper.selectList(invoiceApplyHeaderDTO);
        if (invoiceApplyHeaders.size() == 0) {
            return null;
        }
        return invoiceApplyHeaders.get(0);
    }

    private  void setUserCondition(InvoiceApplyHeaderDTO invoiceApplyHeaderDTO){
        String userJson = iamRemoteService.selectSelf().getBody();
        JSONObject jsonObject= new JSONObject(userJson);

        boolean tenantAdminFlag = false;
        Long organizationId = null;
        Long userId = null;
        if (jsonObject.has(Constants.REMOTE_SERVICE_TENANT_ADMIN_FLAG)){
            tenantAdminFlag=jsonObject.getBoolean(Constants.REMOTE_SERVICE_TENANT_ADMIN_FLAG);
        } else if (jsonObject.has(Constants.REMOTE_SERVICE_TENANT_SUPER_ADMIN_FLAG)) {
            tenantAdminFlag=jsonObject.getBoolean(Constants.REMOTE_SERVICE_TENANT_SUPER_ADMIN_FLAG);
        }

        if(jsonObject.has(Constants.REMOTE_SERVICE_TENANT_ID)){
            organizationId=jsonObject.getLong(Constants.REMOTE_SERVICE_TENANT_ID);
        }

        if(jsonObject.has(Constants.REMOTE_SERVICE_USER_ID)){
            userId= jsonObject.getLong(Constants.REMOTE_SERVICE_USER_ID);
        }

        invoiceApplyHeaderDTO.setTenantAdminFlag(tenantAdminFlag);
        invoiceApplyHeaderDTO.setTenantId(organizationId);
        invoiceApplyHeaderDTO.setCreatedBy(userId);
    }
}

