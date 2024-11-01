package com.hand.demo.app.service.impl;

import com.hand.demo.app.service.FileService;
import com.hand.demo.api.dto.DeleteRequest;
import com.hand.demo.api.dto.DownloadRequest;
import com.hand.demo.api.dto.FileRequest;
import com.hand.demo.api.dto.GetAttachmentRequest;
import lombok.AllArgsConstructor;
import org.hzero.boot.file.FileClient;
import org.hzero.boot.file.dto.FileDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@AllArgsConstructor
public class FileServiceImpl implements FileService {

    private FileClient fileClient;

    @Override
    public String uploadFile(Long organizationId, FileRequest request) {
        String url = null;
        try {
            url = fileClient.uploadFile(
                    organizationId,
                    request.getBucketName(),
                    request.getDirectory(),
                    request.getFileName(),
                    request.getFileType(),
                    request.getStorageCode(),
                    request.getFile().getBytes()
            );
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return url;
    }

    @Override
    public byte[] downloadFile(Long organizationId, DownloadRequest request) {
        try (InputStream fileStream = fileClient.downloadFile(organizationId, request.getBucketName(), request.getUrl());
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            byte[] data = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            byte[] fileBytes = buffer.toByteArray();
            return fileBytes;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteFileByUrl(Long organizationId, DeleteRequest request) {
        fileClient.deleteFileByUrl(
                organizationId,
                request.getBucketName(),
                request.getStorageCode(),
                request.getUrls()
        );
    }

    @Override
    public List<FileDTO> getAttachmentFiles(Long organizationId, GetAttachmentRequest request) {
        return fileClient.getAttachmentFiles(
                organizationId,
                request.getBucketName(),
                request.getAttachmentUUIDs()
        );
    }
}

