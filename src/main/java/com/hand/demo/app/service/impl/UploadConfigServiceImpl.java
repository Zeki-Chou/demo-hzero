package com.hand.demo.app.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.UploadConfigService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.UploadConfig;
import com.hand.demo.domain.repository.UploadConfigRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件上传配置(UploadConfig)应用服务
 *
 * @author
 * @since 2024-11-13 09:44:59
 */
@Service
public class UploadConfigServiceImpl implements UploadConfigService {
    @Autowired
    private UploadConfigRepository uploadConfigRepository;

    @Override
    public Page<UploadConfig> selectList(PageRequest pageRequest, UploadConfig uploadConfig) {
        return PageHelper.doPageAndSort(pageRequest, () -> uploadConfigRepository.selectList(uploadConfig));
    }

    @Override
    public void saveData(List<UploadConfig> uploadConfigs) {
        List<UploadConfig> insertList = uploadConfigs.stream().filter(line -> line.getUploadConfigId() == null).collect(Collectors.toList());
        List<UploadConfig> updateList = uploadConfigs.stream().filter(line -> line.getUploadConfigId() != null).collect(Collectors.toList());
        uploadConfigRepository.batchInsertSelective(insertList);
        uploadConfigRepository.batchUpdateByPrimaryKeySelective(updateList);
    }
}

