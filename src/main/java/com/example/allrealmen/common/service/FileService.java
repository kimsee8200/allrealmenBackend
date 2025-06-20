package com.example.allrealmen.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileService {
    
    @Value("${file.upload.base-dir:uploads}")
    private String baseUploadDir;
    
    /**
     * 단일 파일을 저장하고 접근 URL을 반환합니다.
     */
    public String saveFile(MultipartFile file, String directory) {
        try {
            String uploadDir = baseUploadDir + "/" + directory;
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String filename = generateUniqueFilename(file.getOriginalFilename());
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            return "/files/" + directory + "/" + filename;
        } catch (IOException e) {
            log.error("파일 저장 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }
    }
    
    /**
     * 여러 파일을 저장하고 접근 URL 목록을 반환합니다.
     */
    public List<String> saveFiles(List<MultipartFile> files, String directory) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                urls.add(saveFile(file, directory));
            }
        }
        return urls;
    }
    
    /**
     * 파일을 삭제합니다.
     */
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return;
            }
            
            String relativePath = fileUrl.replace("/files/", "");
            Path filePath = Paths.get(baseUploadDir, relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("파일 삭제 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("파일 삭제에 실패했습니다.", e);
        }
    }
    
    /**
     * 여러 파일을 삭제합니다.
     */
    public void deleteFiles(List<String> fileUrls) {
        if (fileUrls != null) {
            fileUrls.forEach(this::deleteFile);
        }
    }
    
    private String generateUniqueFilename(String originalFilename) {
        return UUID.randomUUID() + "_" + originalFilename;
    }
} 