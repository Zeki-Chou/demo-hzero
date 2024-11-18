package com.hand.demo.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.UploadConfig;

import java.util.List;

/**
 * 文件上传配置(UploadConfig)应用服务
 *
 * @author
 * @since 2024-11-13 09:36:24
 */
public interface UploadConfigService {

    /**
     * 查询数据
     *
     * @param pageRequest   分页参数
     * @param uploadConfigs 查询条件
     * @return 返回值
     */
    Page<UploadConfig> selectList(PageRequest pageRequest, UploadConfig uploadConfigs);

    /**
     * 保存数据
     *
     * @param uploadConfigs 数据
     */
    void saveData(List<UploadConfig> uploadConfigs);

}

