package com.hand.demo.app.service.impl;

import com.hand.demo.app.service.FileService;
import org.hzero.boot.file.FileClient;
import org.hzero.boot.file.dto.FileDTO;
import org.hzero.core.base.BaseAppService;
import org.hzero.core.util.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

@Service
public class FileServiceImpl extends BaseAppService implements FileService {
    @Autowired
    FileClient fileClient;

    private String URI = "";
    private InputStream file = null;

    @Override
    public String fileUpload(Long organizationId, String bucketName, String directory, String fileName, String fileType, String storageCode, MultipartFile byteFile) {
        try {
            /*
                 BATCH NO WILL BE $
                 this.URI = fileClient.uploadFile(organizationId,bucketName, directory, fileName, fileType, storageCode, byteFile.getBytes());
             */

            this.URI = fileClient.uploadAttachment(organizationId,bucketName, directory, UUIDUtils.generateUUID(),fileName, fileType, storageCode,
                    byteFile.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.URI;
    }

    @Override
    public InputStream download(Long organizationId, String bucketName, String url) {
        try {
            InputStream fileStream = fileClient.downloadFile(organizationId, bucketName, url);
            if (fileStream == null) {
                throw new FileNotFoundException("File not found at the specified URL.");
            }
            return fileStream;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String deleteFileByUrl(Long organizationId, String bucketName, String storageCode, List<String> urls) {
        try {

            if (urls == null) {
                return "Null";
            }

            fileClient.deleteFileByUrl(organizationId, bucketName, storageCode, urls);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Success";
    }

    @Override
    public List<FileDTO> getAttachmentFiles(Long organizationId, String bucketName, List<String> attachmentUUIDs) {
        List<FileDTO> fileEntities = fileClient.getAttachmentFiles(organizationId, bucketName, attachmentUUIDs);
        return fileEntities;
    }


}
