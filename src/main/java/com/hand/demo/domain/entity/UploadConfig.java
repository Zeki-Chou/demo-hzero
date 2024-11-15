package com.hand.demo.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * 文件上传配置(UploadConfig)实体类
 *
 * @author azhar.naufal@hand-global.com
 * @since 2024-11-13 11:18:53
 */

@Getter
@Setter
@ApiModel("文件上传配置")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "hfle_upload_config")
public class UploadConfig extends AuditDomain {
    private static final long serialVersionUID = -64004269710818239L;

    public static final String FIELD_UPLOAD_CONFIG_ID = "uploadConfigId";
    public static final String FIELD_BUCKET_NAME = "bucketName";
    public static final String FIELD_CONTENT_TYPE = "contentType";
    public static final String FIELD_DIRECTORY = "directory";
    public static final String FIELD_FILE_FORMAT = "fileFormat";
    public static final String FIELD_STORAGE_SIZE = "storageSize";
    public static final String FIELD_STORAGE_UNIT = "storageUnit";
    public static final String FIELD_TENANT_ID = "tenantId";

    @Id
    @GeneratedValue
    private Long uploadConfigId;

    @ApiModelProperty(value = "文件目录", required = true)
    @NotBlank
    private String bucketName;

    @ApiModelProperty(value = "文件分类，值集HFLE.CONTENT_TYPE")
    private String contentType;

    @ApiModelProperty(value = "上传目录")
    private String directory;

    @ApiModelProperty(value = "文件格式，文件分类子值集HFLE.FILE_FORMAT")
    private String fileFormat;

    @ApiModelProperty(value = "存储大小", required = true)
    @NotNull
    private Integer storageSize;

    @ApiModelProperty(value = "存储大小限制单位，值集HFLE.STORAGE_UNTT,KB/MB", required = true)
    @NotBlank
    private String storageUnit;

    @ApiModelProperty(value = "租户ID，hpfm_tenant.tenant_id", required = true)
    @NotNull
    private Long tenantId;


}

