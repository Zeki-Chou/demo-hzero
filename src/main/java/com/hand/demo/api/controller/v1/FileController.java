package com.hand.demo.api.controller.v1;

import com.hand.demo.app.service.FileService;
import com.hand.demo.api.dto.DeleteRequest;
import com.hand.demo.api.dto.DownloadRequest;
import com.hand.demo.api.dto.FileRequest;
import com.hand.demo.api.dto.GetAttachmentRequest;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.hzero.boot.file.dto.FileDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController("fileController.v1")
@RequestMapping("/v1/{organizationId}")
@AllArgsConstructor
public class FileController {

    private FileService fileService;

    @ApiOperation(value = "upload-file-to-minio")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/upload")
    public String uploadFile(
            @PathVariable("organizationId") Long organizationId,
            @RequestParam String bucketName,
            @RequestParam String directory,
            @RequestParam String fileName,
            @RequestParam String fileType,
            @RequestParam String storageCode,
            @RequestParam("file") MultipartFile file) {

        FileRequest request = FileRequest.builder()
                .bucketName(bucketName)
                .directory(directory)
                .fileName(fileName)
                .fileType(fileType)
                .storageCode(storageCode)
                .file(file)
                .build();

        return fileService.uploadFile(organizationId, request);
    }

    @ApiOperation(value = "download-file")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable("organizationId") Long organizationId,
            @RequestParam String bucketName,
            @RequestParam String url) {

        DownloadRequest request = DownloadRequest.builder()
                .bucketName(bucketName)
                .url(url)
                .build();

        byte[] downloadedFile = fileService.downloadFile(organizationId, request);

        if (downloadedFile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "tes.txt");
        return new ResponseEntity<>(downloadedFile, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "delete-file")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/delete")
    public String deleteFile(
            @PathVariable("organizationId") Long organizationId,
            @RequestParam String bucketName,
            @RequestParam List<String> url) {

        DeleteRequest request = DeleteRequest.builder()
                .bucketName(bucketName)
                .urls(url)
                .build();

        fileService.deleteFileByUrl(organizationId, request);

        return "Success";
    }

    @ApiOperation(value = "get-attachments-file")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/attachments")
    public List<FileDTO> getAttachmentsFile(
            @PathVariable("organizationId") Long organizationId,
            @RequestParam String bucketName,
            @RequestParam List<String> attachmentUUIDs) {

        GetAttachmentRequest request = GetAttachmentRequest.builder()
                .bucketName(bucketName)
                .attachmentUUIDs(attachmentUUIDs)
                .build();

        return fileService.getAttachmentFiles(organizationId, request);
    }
}
