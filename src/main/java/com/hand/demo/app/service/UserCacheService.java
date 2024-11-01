package com.hand.demo.app.service;

import com.hand.demo.api.dto.UserCacheDTO;

public interface UserCacheService {
    public UserCacheDTO getUserFromRedis(Long userId);
}