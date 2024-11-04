package com.hand.demo.api.controller.v1;

import com.hand.demo.app.service.FileService;
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

import java.util.List;

@RestController("fileController.v1")
@RequestMapping("/v1/{organizationId}/files" )
public class FileController extends BaseController {
    private final FileService service;

    @Autowired
    public FileController(FileService service) {
        this.service = service;
    }

    @ApiOperation(value = "Upload File")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@PathVariable Long organizationId, @RequestParam String bucketName,
                                      @RequestParam String directory, @RequestParam String fileName,
                                      @RequestParam String fileType, @RequestParam String storageCode, MultipartFile multipartFile) {
        String url = service.upload(organizationId, bucketName, directory, fileName, fileType, storageCode, multipartFile);
        return Results.success(url);
    }

    @ApiOperation(value = "Download File")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/download")
    public ResponseEntity<String> downloadFile(@PathVariable Long organizationId, String bucketName, String url) {
        service.download(organizationId, bucketName, url);
        return Results.success(url);
    }

    @ApiOperation(value = "Delete File")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@PathVariable Long organizationId, String bucketName, @RequestParam List<String> urls) {
        service.delete(organizationId, bucketName, urls);
        return Results.success("file Deleted");
    }

    @ApiOperation(value = "Find Files")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/find")
    public ResponseEntity<List<FileDTO>> findFile(@PathVariable Long organizationId, String bucketName, @RequestParam List<String> attachmentUUIDSs) {
        List<FileDTO> files = service.find(organizationId, bucketName, attachmentUUIDSs);
        return Results.success(files);
    }
}
