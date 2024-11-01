package com.hand.demo.api.controller.v1;

import com.hand.demo.app.service.DemoFileService;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.file.dto.FileDTO;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * FILE API
 *
 * @author lareza.farhan@hand-global.com 2024-10-17 14:07:13
 */
@RestController("demoFileController.v1" )
@RequestMapping("/v1/{organizationId}/files" )
public class DemoFileController extends BaseController {

    DemoFileService demoFileService;

    @Autowired
    public void setDemoFileService(DemoFileService demoFileService) {
        this.demoFileService = demoFileService;
    }

    @ApiOperation(value = "Upload File")
    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@PathVariable Long organizationId, @RequestParam String bucketName, @RequestParam String directory, @RequestParam String fileName, @RequestParam String fileType, @RequestParam String storageCode,@RequestParam MultipartFile file){
        String url = demoFileService.upload(organizationId,bucketName,directory,fileName,fileType,storageCode,file);
        return Results.success( "Upload Success. Url: "+url);
    }

    @ApiOperation(value = "Download File")
    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @GetMapping("/download")
    public ResponseEntity<String> download(@PathVariable Long organizationId, @RequestParam String bucketName, @RequestParam String url, @RequestParam String downloadFolder){
        String path = demoFileService.download(organizationId,bucketName,url,downloadFolder);
        return Results.success( "Download Success. Path: "+path);
    }

    @ApiOperation(value = "Delete File By Urls")
    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @DeleteMapping("/deleteUrls")
    public ResponseEntity<String> delete(@PathVariable Long organizationId,@RequestParam String bucketName,@RequestParam String storageCode,@RequestParam List<String> urls){
        demoFileService.delete(organizationId,bucketName,storageCode,urls);
        return Results.success( "Delete Success. Deleted urls: "+urls.toString());
    }

    @ApiOperation(value = "Find Files By UUIDs")
    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @GetMapping("/findUUIDs")
    public ResponseEntity<List<FileDTO>> find(@PathVariable Long organizationId,@RequestParam String bucketName,@RequestParam List<String> attachmentUUIDs){
        List<FileDTO> fileDTOS = demoFileService.find(organizationId,bucketName,attachmentUUIDs);
        return Results.success(fileDTOS);
    }
}