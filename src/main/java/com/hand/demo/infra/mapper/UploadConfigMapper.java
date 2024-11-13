package com.hand.demo.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import com.hand.demo.domain.entity.UploadConfig;

import java.util.List;

/**
 * 文件上传配置(UploadConfig)应用服务
 *
 * @author
 * @since 2024-11-13 09:40:19
 */
public interface UploadConfigMapper extends BaseMapper<UploadConfig> {
    /**
     * 基础查询
     *
     * @param uploadConfig 查询条件
     * @return 返回值
     */
    List<UploadConfig> selectList(UploadConfig uploadConfig);
}

