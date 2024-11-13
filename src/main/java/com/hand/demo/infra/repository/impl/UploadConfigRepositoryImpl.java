package com.hand.demo.infra.repository.impl;

import org.apache.commons.collections.CollectionUtils;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.UploadConfig;
import com.hand.demo.domain.repository.UploadConfigRepository;
import com.hand.demo.infra.mapper.UploadConfigMapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * 文件上传配置(UploadConfig)资源库
 *
 * @author
 * @since 2024-11-13 09:40:20
 */
@Component
public class UploadConfigRepositoryImpl extends BaseRepositoryImpl<UploadConfig> implements UploadConfigRepository {
    @Resource
    private UploadConfigMapper uploadConfigMapper;

    @Override
    public List<UploadConfig> selectList(UploadConfig uploadConfig) {
        return uploadConfigMapper.selectList(uploadConfig);
    }

    @Override
    public UploadConfig selectByPrimary(Long uploadConfigId) {
        UploadConfig uploadConfig = new UploadConfig();
        uploadConfig.setUploadConfigId(uploadConfigId);
        List<UploadConfig> uploadConfigs = uploadConfigMapper.selectList(uploadConfig);
        if (uploadConfigs.size() == 0) {
            return null;
        }
        return uploadConfigs.get(0);
    }

}

