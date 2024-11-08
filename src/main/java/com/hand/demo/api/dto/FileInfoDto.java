package com.hand.demo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
public class FileInfoDto {
    private String bucketName;

    private String directory;

    private String fileName;

    private String fileType;

    private String storageCode;

    private MultipartFile byteFile;
}
