package com.hand.demo.app.service.impl;

import com.hand.demo.app.service.FileService;
import com.hand.demo.infra.constant.FileConstant;
import org.hzero.boot.file.FileClient;
import org.hzero.boot.file.dto.FileDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {

    FileClient client;

    public FileServiceImpl(FileClient client) {
        this.client = client;
    }

    @Override
    public String upload(Long organizationalId, String bucketName, String directory, String fileName,
                           String fileType, String storageCode, MultipartFile multipartFile) {

        String url = null;

        try {
            url = client.uploadFile(organizationalId, bucketName, directory, fileName, fileType, storageCode, multipartFile.getBytes());
        } catch (IOException e) {
            System.out.println("problem uploading file");
        }

        return url;
    }

    @Override
    public void delete(Long organizationId, String bucketName, List<String> urls) {
        client.deleteFileByUrl(organizationId, bucketName, urls);
    }

    @Override
    public List<FileDTO> find(Long organizationId, String bucketName, List<String> attachmentUUIDSs) {
        return client.getAttachmentFiles(organizationId, bucketName, attachmentUUIDSs);
    }

    @Override
    public void download(Long organizationId, String bucketName, String url) {
        try(InputStream inputStream = client.downloadFile(organizationId, bucketName, url);) {
            File targetFile = new File(FileConstant.FILE_DOWNLOAD_PATH);
            Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("download should be successful");
        } catch (Exception e) {
            System.out.println("there is a problem downloading the file");
        }
    }


}
