package com.hand.demo.app.service;

import org.hzero.boot.file.dto.FileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface FileService {
    String uploadFile(Long organizationId, String bucketName, String directory, String fileName, String fileType, String storageCode, MultipartFile multipartfile);

    InputStream downloadFile(Long organizationId, String bucketName, String url);

    String deleteFile(Long organizationId, String bucketName, String storageCode, List<String> urls);

    List<FileDTO> getAttachment(Long organizationId, String bucketName, List<String> attachmentUUIDs);
}
