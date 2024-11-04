package com.hand.demo.app.service;

import org.hzero.boot.file.dto.FileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    String upload(Long organizationalId, String bucketName, String directory, String fileName, String fileType, String storageCode, MultipartFile multipartFile);
    void delete(Long organizationId, String bucketName, List<String> urls);
    List<FileDTO> find(Long organizationId, String bucketName, List<String> attachmentUUIDSs);
    void download(Long organizationId, String bucketName, String url);
}
