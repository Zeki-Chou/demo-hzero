package com.hand.demo.app.service.impl;

import com.hand.demo.api.dto.FileInfoDto;
import com.hand.demo.app.service.FileService;
import org.hzero.boot.file.FileClient;
import org.hzero.boot.file.dto.FileDTO;
import org.hzero.core.base.BaseAppService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

@Service
public class FileServiceImpl extends BaseAppService implements FileService {
    private final FileClient fileClient;
    public FileServiceImpl(FileClient fileClient) {
        this.fileClient = fileClient;
    }

    @Override
    public String fileUpload(Long organizationId, FileInfoDto fileInfoDto) {
        String url = null;
        try {
            url = fileClient.uploadFile(organizationId, fileInfoDto.getBucketName(), fileInfoDto.getFileType(), fileInfoDto.getFileName(), fileInfoDto.getByteFile().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return url;
    }

    @Override
    public byte[] fileDownload(Long organizationId, String bucketName, String url) {
        try (InputStream inputStream = fileClient.downloadFile(organizationId, bucketName, url)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            return buffer.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error downloading file from bucket: " + bucketName, e);
        }


    }

    @Override
    public String fileDeleteUrl(Long organizationId, String bucketName, String storageCode, List<String> urls) {
        if (urls == null) {
           return "Failed";
        }

        fileClient.deleteFileByUrl(organizationId, bucketName, storageCode, urls);
        return "Success";
    }

    @Override
    public List<FileDTO> getAttachment(Long organizationId, String bucketName, List<String> attachmentUUIDs) {

        return fileClient.getAttachmentFiles(organizationId, bucketName, attachmentUUIDs);
    }


}
