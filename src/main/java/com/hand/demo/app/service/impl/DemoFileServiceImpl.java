package com.hand.demo.app.service.impl;

import com.hand.demo.app.service.DemoFileService;
import io.choerodon.core.exception.CommonException;
import org.hzero.boot.file.FileClient;
import org.hzero.boot.file.dto.FileDTO;
import org.hzero.core.util.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class DemoFileServiceImpl implements DemoFileService {
    private final FileClient fileClient;

    @Autowired
    public DemoFileServiceImpl(FileClient fileClient){
        this.fileClient=fileClient;
    }

    @Override
    public String upload(Long organizationId, String bucketName, String directory, String fileName, String fileType, String storageCode, MultipartFile file){
        byte[] fileBytes = null;
        String url = null;
        try {
            fileBytes = file.getBytes();
            url = fileClient.uploadAttachment(organizationId,bucketName,directory,UUIDUtils.generateUUID()
                    ,fileName,fileType,storageCode,fileBytes);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
        return url;
    }

    @Override
    public String download(Long organizationId, String bucketName, String url, String downloadFolder) {
        String filePath = null;

        try (InputStream inputStream = fileClient.downloadFile(organizationId, bucketName, url)) {

            String fileName = url.substring(url.lastIndexOf("@") + 1);
            Path downloadFolderPath = Paths.get(downloadFolder);
            Files.createDirectories(downloadFolderPath);

            Path targetFilePath = downloadFolderPath.resolve(fileName);
            try (FileOutputStream outputStream = new FileOutputStream(targetFilePath.toFile())) {
                StreamUtils.copy(inputStream, outputStream);
            }

            filePath = targetFilePath.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommonException(e.getMessage());
        }

        return filePath;
    }

    @Override
    public void delete(Long organizationId, String bucketName, String storageCode, List<String> urls){
        try {
            fileClient.deleteFileByUrl(organizationId,bucketName,storageCode,urls);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public List<FileDTO> find(Long organizationId, String bucketName, List<String> attachmentUUIDs){
        List<FileDTO> fileDTOS = null;
        try {
            fileDTOS = fileClient.getAttachmentFiles(organizationId,bucketName,attachmentUUIDs);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
        return fileDTOS;
    }
}
