package com.hand.demo.domain.repository;

import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.UploadConfig;

import java.util.List;

/**
 * 文件上传配置(UploadConfig)资源库
 *
 * @author azhar.naufal@hand-global.com
 * @since 2024-11-13 11:18:54
 */
public interface UploadConfigRepository extends BaseRepository<UploadConfig> {
    /**
     * 查询
     *
     * @param uploadConfig 查询条件
     * @return 返回值
     */
    List<UploadConfig> selectList(UploadConfig uploadConfig);

    /**
     * 根据主键查询（可关联表）
     *
     * @param uploadConfigId 主键
     * @return 返回值
     */
    UploadConfig selectByPrimary(Long uploadConfigId);
}
