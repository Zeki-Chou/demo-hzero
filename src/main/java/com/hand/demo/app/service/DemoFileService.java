package com.hand.demo.app.service;

import org.hzero.boot.file.dto.FileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DemoFileService {
    String upload(Long organizationId, String bucketName, String directory, String fileName, String fileType, String storageCode, MultipartFile byteFile);
    String download(Long organizationId, String bucketName, String url, String downloadFolder);
    void delete(Long organizationId, String bucketName, String storageCode, List<String> urls);
    List<FileDTO> find(Long organizationId, String bucketName, List<String> attachmentUUIDs);
}
