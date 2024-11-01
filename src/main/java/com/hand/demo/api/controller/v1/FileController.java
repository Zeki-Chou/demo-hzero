package com.hand.demo.api.controller.v1;

import com.hand.demo.app.service.FileService;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.file.dto.FileDTO;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.List;

@RestController("fileController.v1" )
@RequestMapping("/v1/{organizationId}/files" )
public class FileController extends BaseController {
    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @ApiOperation(value = "uploadFile")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("uploadFile")
    public ResponseEntity<String> uploadFile(@PathVariable Long organizationId,
                                              @RequestParam String bucketName,
                                              @RequestParam String directory,
                                              @RequestParam String fileName,
                                              @RequestParam String fileType,
                                              @RequestParam String storageCode,
                                              @RequestParam("multipartfile") MultipartFile multipartfile) {

        String url = fileService.uploadFile(organizationId, bucketName, directory, fileName, fileType, storageCode, multipartfile);
        return Results.success(url);
    }

    @ApiOperation(value = "downloadFile")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("downloadFile")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long organizationId,
                                                 @RequestParam String bucketName,
                                                 @RequestParam String url) {
        InputStream fileStream = fileService.downloadFile(organizationId, bucketName, url);

        if (fileStream == null) {
            return ResponseEntity.notFound().build(); // Handle case where file is not found
        }

        // Create a Resource from the InputStream
        Resource resource = new InputStreamResource(fileStream);

        // You can set the filename in the Content-Disposition header if needed
        String filename = url.substring(url.lastIndexOf('/') + 1); // Extract filename from URL

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM) // Set content type as appropriate
                .body(resource);
    }

    @ApiOperation(value = "deleteFile")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("deleteFile")
    public ResponseEntity<String> deleteFile(@PathVariable Long organizationId,
                                             @RequestParam String bucketName,
                                             @RequestParam String storageCode,
                                             @RequestParam List<String> urls) {
        String result = fileService.deleteFile(organizationId, bucketName, storageCode, urls);

        return Results.success(result);
    }

    @ApiOperation(value = "getAttachmentFile")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("getAttachmentFile")
    public ResponseEntity<List<FileDTO>> getAttachment(@PathVariable Long organizationId,
                                       @RequestParam String bucketName,
                                       @RequestParam List<String> attachmentUUIDs) {
        List<FileDTO> fileDTOList = fileService.getAttachment(organizationId, bucketName, attachmentUUIDs);

        return Results.success(fileDTOList);
    }

}
