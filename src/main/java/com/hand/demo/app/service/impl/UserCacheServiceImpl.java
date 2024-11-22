package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.UserCacheDTO;
import com.hand.demo.app.service.UserCacheService;
import com.hand.demo.domain.repository.UserRepository;
import org.hzero.core.base.BaseAppService;
import org.hzero.core.cache.ProcessCacheValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserCacheServiceImpl extends BaseAppService implements UserCacheService {
    @Autowired
    private UserRepository userRepository;

    @ProcessCacheValue
    public UserCacheDTO getUserFromRedis(Long userId) {
        UserCacheDTO userCacheDTO = new UserCacheDTO();
        userCacheDTO.setUser_id(userId);
        return userCacheDTO;
    }
}