package com.hand.demo.infra.repository.impl;

import org.apache.commons.collections.CollectionUtils;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.IamUser;
import com.hand.demo.domain.repository.IamUserRepository;
import com.hand.demo.infra.mapper.IamUserMapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户(IamUser)资源库
 *
 * @author Allan
 * @since 2024-11-19 09:26:53
 */
@Component
public class IamUserRepositoryImpl extends BaseRepositoryImpl<IamUser> implements IamUserRepository {
    @Resource
    private IamUserMapper iamUserMapper;

    @Override
    public List<IamUser> selectList(IamUser iamUser) {
        return iamUserMapper.selectList(iamUser);
    }

    @Override
    public IamUser selectByPrimary(Long id) {
        IamUser iamUser = new IamUser();
        iamUser.setId(id);
        List<IamUser> iamUsers = iamUserMapper.selectList(iamUser);
        if (iamUsers.isEmpty()) {
            return null;
        }
        return iamUsers.get(0);
    }

}

