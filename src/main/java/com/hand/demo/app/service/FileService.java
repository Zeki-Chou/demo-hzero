package com.hand.demo.app.service;

import org.hzero.boot.file.dto.FileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface FileService {
    String fileUpload(Long organizationId, String bucketName, String directory, String fileName, String fileType, String storageCode, MultipartFile byteFile);
    InputStream download(Long organizationId, String bucketName, String url);
    String deleteFileByUrl(Long organizationId, String bucketName, String storageCode, List<String> urls);
    List<FileDTO> getAttachmentFiles(Long organizationId, String bucketName, List<String> attachmentUUIDs);
}
