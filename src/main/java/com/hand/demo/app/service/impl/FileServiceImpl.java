package com.hand.demo.app.service.impl;

import com.hand.demo.app.service.FileService;
import org.hzero.boot.file.FileClient;
import org.hzero.boot.file.dto.FileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private FileClient fileClient;
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String uploadFile(Long organizationId, String bucketName, String directory, String fileName, String fileType, String storageCode, MultipartFile multipartfile){
        String url = null;
        try {
            url = fileClient.uploadFile(organizationId, bucketName, directory, fileName, fileType, storageCode, multipartfile.getBytes());
        }catch (IOException e){
            e.printStackTrace();
        }
        return url;
    }

        @Override
        public InputStream downloadFile(Long organizationId, String bucketName, String url){
            InputStream fileDownloaded = null;
            try {
                fileDownloaded = fileClient.downloadFile(organizationId, bucketName, url);
                if (fileDownloaded == null) {
                    logger.error("Downloaded file is null for URL: {}", url);
                }
            } catch (Exception e) {
                logger.error("Error downloading file: {}", e.getMessage(), e);
            }
            return fileDownloaded;
        }

        @Override
        public String deleteFile(Long organizationId, String bucketName, String storageCode, List<String> urls){
            String result = "";
            try {
                fileClient.deleteFileByUrl(organizationId, bucketName, storageCode, urls);
                result = "success deleted";
            }catch (Exception e){
                result = e.getMessage();
            }

            return result;
        }

        @Override
        public List<FileDTO> getAttachment(Long organizationId, String bucketName, List<String> attachmentUUIDs){
            List<FileDTO> fileDTOList = fileClient.getAttachmentFiles(organizationId, bucketName, attachmentUUIDs);

            return fileDTOList;
        }


}
