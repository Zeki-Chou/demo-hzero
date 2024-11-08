package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.FileInfoDto;
import com.hand.demo.app.service.FileService;
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
@RequestMapping("/v1/{organizationId}/file")
@AllArgsConstructor
public class FileController {
    private FileService fileService;

    @ApiOperation(value = "Upload File")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/upload")
    public String uploadFile (@RequestParam String fileName, @RequestParam String bucketName, @RequestParam String directory, @RequestParam String fileType, @RequestParam String storageCode, @RequestParam MultipartFile byteFile, @PathVariable Long organizationId){
        FileInfoDto fileDto = new FileInfoDto(bucketName, directory, fileName, fileType, storageCode, byteFile);
        String url = fileService.fileUpload(organizationId, fileDto);

        return url;
    }

    @ApiOperation(value = "Download File")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam String bucketName,
                                               @RequestParam String url,
                                               @PathVariable Long organizationId) {
        byte[] fileData = fileService.fileDownload(organizationId, bucketName, url);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "download.txt");
        headers.setContentLength(fileData.length);

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "Delete File")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam String bucketName, @RequestParam String storageCode,
                                             @RequestParam List<String> urls, @PathVariable Long organizationId){

        String results =  fileService.fileDeleteUrl(organizationId, bucketName, storageCode, urls);
        if (results == "Failed") {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().body("Success");
    }

    @ApiOperation(value = "Get Attachment")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/get")
    public List<FileDTO> getAttachment (@RequestParam String bucketName, @RequestParam List<String> attachmentUUIDs, @PathVariable Long organizationId){

        return fileService.getAttachment(organizationId,  bucketName, attachmentUUIDs);
    }
}
