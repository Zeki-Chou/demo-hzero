package com.hand.demo.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hand.demo.app.service.UploadConfigService;
import com.hand.demo.domain.entity.UploadConfig;
import com.hand.demo.domain.repository.UploadConfigRepository;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * UploadConfig
 *
 * @author
 * @since 2024-11-13 09:36:24
 */

@RestController("uploadConfigController.v1")
@RequestMapping("/v1/{organizationId}/upload-configs")
public class UploadConfigController extends BaseController {

    @Autowired
    private UploadConfigRepository uploadConfigRepository;

    @Autowired
    private UploadConfigService uploadConfigService;

    @ApiOperation(value = "List")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<Page<UploadConfig>> list(UploadConfig uploadConfig, @PathVariable Long organizationId,
                                                   @ApiIgnore @SortDefault(value = UploadConfig.FIELD_UPLOAD_CONFIG_ID,
                                                           direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<UploadConfig> list = uploadConfigService.selectList(pageRequest, uploadConfig);
        return Results.success(list);
    }

    @ApiOperation(value = "Detail")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{uploadConfigId}/detail")
    public ResponseEntity<UploadConfig> detail(@PathVariable Long uploadConfigId) {
        UploadConfig uploadConfig = uploadConfigRepository.selectByPrimary(uploadConfigId);
        return Results.success(uploadConfig);
    }

    @ApiOperation(value = "Save")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<UploadConfig>> save(@PathVariable Long organizationId, @RequestBody List<UploadConfig> uploadConfigs) {
        validObject(uploadConfigs);
        SecurityTokenHelper.validTokenIgnoreInsert(uploadConfigs);
        uploadConfigs.forEach(item -> item.setTenantId(organizationId));
        uploadConfigService.saveData(uploadConfigs);
        return Results.success(uploadConfigs);
    }

    @ApiOperation(value = "Remove")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<UploadConfig> uploadConfigs) {
        SecurityTokenHelper.validToken(uploadConfigs);
        uploadConfigRepository.batchDeleteByPrimaryKey(uploadConfigs);
        return Results.success();
    }

}

