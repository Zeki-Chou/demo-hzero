package com.hand.demo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileRequest {
    private String bucketName;
    private String directory;
    private String fileName;
    private String fileType;
    private String storageCode;
    private MultipartFile file;
}
