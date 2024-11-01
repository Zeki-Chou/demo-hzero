package com.hand.demo.api.controller.v1;

import com.hand.demo.app.service.impl.FileServiceImpl;
import com.hand.demo.config.SwaggerTags;
import com.hand.demo.domain.entity.Task;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.file.dto.FileDTO;
import org.hzero.common.HZeroService;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Api(tags = SwaggerTags.FILE)
@RestController("fileController.v1")
@RequestMapping("/v1/{organizationId}/files")
public class FileController {
    @Autowired
    FileServiceImpl fileService;

    @ApiOperation(value = "UPLOAD FILE")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/upload")
    public String upload(@PathVariable Long organizationId, @RequestParam String bucketName,
                         @RequestParam String directory, @RequestParam String fileName, @RequestParam  String fileType,
                         @RequestParam String storageCode, MultipartFile byteFile) {
        String url = fileService.fileUpload(organizationId, bucketName,
                directory, fileName, fileType, storageCode, byteFile);
        return url;
    }

    @ApiOperation(value = "DOWNLOAD FILE")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long organizationId,
                                                        @RequestParam String bucketName, @RequestParam String url) {
        InputStream file = fileService.download(organizationId, bucketName, url);

        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test.txt\"");

        try {
            String filePath = "C:\\Users\\Allan Sugianto\\Downloads\test.txt";

            Files.copy(file, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(file));
    }

    @ApiOperation(value = "DELETE FILE")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@PathVariable Long organizationId, @RequestParam String bucketName,
                                         @RequestParam String storageCode, @RequestParam List<String> url) {
        String result = fileService.deleteFileByUrl(organizationId, bucketName, storageCode ,url);

        if (result == "Null") {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok().body("Success");
    }

    @ApiOperation(value = "FIND FILE")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/find")
    public ResponseEntity<List<FileDTO>> find(@PathVariable Long organizationId,
                                         @RequestParam String bucketName, @RequestParam List<String> attachmentUUIDs) {
        List<FileDTO> fileDTO = fileService.getAttachmentFiles(organizationId, bucketName, attachmentUUIDs);

        return ResponseEntity.ok().body(fileDTO);
    }
}
