package com.hand.demo.app.service;

import com.hand.demo.api.dto.DeleteRequest;
import com.hand.demo.api.dto.DownloadRequest;
import com.hand.demo.api.dto.FileRequest;
import com.hand.demo.api.dto.GetAttachmentRequest;
import org.hzero.boot.file.dto.FileDTO;

import java.util.List;

public interface FileService {
    String uploadFile(Long organizationId, FileRequest request);

    byte[] downloadFile(Long organizationId, DownloadRequest request);

    void deleteFileByUrl(Long organizationId, DeleteRequest request);

    List<FileDTO> getAttachmentFiles(Long organizationId, GetAttachmentRequest request);
}