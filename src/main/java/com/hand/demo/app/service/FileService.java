package com.hand.demo.app.service;

import com.hand.demo.api.dto.FileInfoDto;
import org.hzero.boot.file.dto.FileDTO;

import java.util.List;

public interface FileService {
    String fileUpload(Long organizationId, FileInfoDto fileDto);

    byte[] fileDownload(Long organizationId, String bucketName, String url);

    String fileDeleteUrl(Long organizationId, String bucketName, String storageCode, List<String> urls);

    List<FileDTO> getAttachment(Long organizationId, String bucketName, List<String> attachmentUUIDs);
}
